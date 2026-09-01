package ru.nstu.nest.inference.source.kb;

import org.springframework.stereotype.Service;
import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Action;
import ru.nstu.nest.inference.dto.Condition;
import ru.nstu.nest.inference.dto.Operator;
import ru.nstu.nest.inference.dto.Rule;
import ru.nstu.nest.inference.source.kb.metadata.FrameDefinition;
import ru.nstu.nest.inference.source.kb.metadata.KnowledgebaseMetadata;
import ru.nstu.nest.inference.source.kb.metadata.SlotDefinition;
import ru.nstu.nest.inference.source.kb.metadata.TargetFactDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KlbParser {

    private static final Pattern STATEMENT_PATTERN =
            Pattern.compile("(?i)^(=|EQ|>|<|GT|LT|NE|!=)\\s*\\((.*)\\)\\s*(\\d+)?$");
    private static final Pattern FRAME_START_PATTERN =
            Pattern.compile("(?i)^frame\\s*=\\s*(.+)$");
    private static final Pattern SLOT_PATTERN =
            Pattern.compile("^(.+?)\\s*:\\s*\\((.*)\\)\\s*$");
    private static final Pattern QUESTION_PATTERN =
            Pattern.compile("^(.*?)\\s*\\[(.*)]\\s*$");
    private static final Pattern NUMERIC_MARKER_PATTERN =
            Pattern.compile("\\s*\\(\\s*численный\\s*\\)\\s*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public KnowledgeBase createKnowledgeBase(String content) {
        return createKnowledgeBase(content, "");
    }

    public KnowledgeBase createKnowledgeBase(String content, String sourceId) {
        return new KnowledgeBase(sourceId, parse(content));
    }

    public KnowledgebaseMetadata parseMetadata(String content, String sourceName) {
        List<FrameDefinition> inputFrames = new ArrayList<>();
        List<TargetFactDefinition> targetFacts = new ArrayList<>();
        FrameBuilder currentFrame = null;

        for (String rawLine : safeContent(content).lines().toList()) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher frameMatcher = FRAME_START_PATTERN.matcher(line);
            if (frameMatcher.matches()) {
                appendFrame(currentFrame, inputFrames, targetFacts);
                currentFrame = new FrameBuilder(frameMatcher.group(1).trim());
                continue;
            }

            if (isFrameEnd(line)) {
                appendFrame(currentFrame, inputFrames, targetFacts);
                currentFrame = null;
                continue;
            }

            if (currentFrame == null || isParentLine(line)) {
                continue;
            }

            parseSlot(line).ifPresent(currentFrame.slots::add);
        }

        appendFrame(currentFrame, inputFrames, targetFacts);

        return new KnowledgebaseMetadata(sourceName, inputFrames, targetFacts);
    }

    List<Rule> parse(String content) {
        List<Rule> rules = new ArrayList<>();
        RuleBuilder currentRule = null;
        boolean actionBlock = false;

        for (String rawLine : safeContent(content).lines().toList()) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (startsRule(line)) {
                currentRule = new RuleBuilder(line);
                actionBlock = false;
                continue;
            }

            if (line.equalsIgnoreCase("do")) {
                actionBlock = true;
                continue;
            }

            if (line.equalsIgnoreCase("endr")) {
                if (currentRule != null) {
                    rules.add(currentRule.build());
                }
                currentRule = null;
                actionBlock = false;
                continue;
            }

            if (currentRule != null) {
                Statement statement = parseStatement(line).orElse(null);
                if (statement != null) {
                    appendStatement(currentRule, statement, actionBlock);
                }
            }
        }

        return rules;
    }

    private String safeContent(String content) {
        return content == null ? "" : content;
    }

    private boolean startsRule(String line) {
        return line.regionMatches(true, 0, "rule", 0, 4);
    }

    private boolean isFrameEnd(String line) {
        return line.equalsIgnoreCase("endf") || line.equalsIgnoreCase("endf;");
    }

    private boolean isParentLine(String line) {
        return line.equalsIgnoreCase("parent:");
    }

    private Optional<SlotDefinition> parseSlot(String line) {
        Matcher matcher = SLOT_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String leftSide = matcher.group(1).trim();
        String rawOptions = matcher.group(2).trim();

        boolean numeric = NUMERIC_MARKER_PATTERN.matcher(leftSide).find();
        leftSide = NUMERIC_MARKER_PATTERN.matcher(leftSide).replaceAll(" ").trim();

        String question = null;
        Matcher questionMatcher = QUESTION_PATTERN.matcher(leftSide);
        if (questionMatcher.matches()) {
            leftSide = questionMatcher.group(1).trim();
            question = questionMatcher.group(2).trim();
        }

        String slotName = leftSide.trim();
        if (slotName.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new SlotDefinition(slotName, question, numeric, parseOptions(rawOptions)));
    }

    private List<String> parseOptions(String rawOptions) {
        if (rawOptions.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rawOptions.split(";"))
                .map(String::trim)
                .filter(option -> !option.isBlank())
                .toList();
    }

    private void appendFrame(
            FrameBuilder frame,
            List<FrameDefinition> inputFrames,
            List<TargetFactDefinition> targetFacts
    ) {
        if (frame == null) {
            return;
        }

        if (isGoalFrame(frame.name)) {
            frame.slots.stream()
                    .map(SlotDefinition::name)
                    .map(this::tryCreateTarget)
                    .flatMap(Optional::stream)
                    .forEach(targetFacts::add);
            return;
        }

        inputFrames.add(new FrameDefinition(frame.name, frame.slots));
    }

    private Optional<TargetFactDefinition> tryCreateTarget(String address) {
        try {
            return Optional.of(new TargetFactDefinition(address));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private boolean isGoalFrame(String frameName) {
        String normalized = frameName.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("goal") || normalized.equals("цель");
    }

    private java.util.Optional<Statement> parseStatement(String line) {
        Matcher matcher = STATEMENT_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return java.util.Optional.empty();
        }

        String[] parts = matcher.group(2).split(";", 2);
        if (parts.length < 2) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new Statement(
                Operator.fromString(matcher.group(1)),
                Address.parse(parts[0].trim()),
                parts[1].trim()
        ));
    }

    private void appendStatement(RuleBuilder rule, Statement statement, boolean actionBlock) {
        if (actionBlock) {
            if (!rule.actions.isEmpty()) {
                throw new IllegalArgumentException(
                        "Правило " + rule.id + " содержит несколько действий. Поддерживается только одно действие."
                );
            }
            rule.actions.add(new Action(statement.address(), statement.value()));
            return;
        }

        rule.conditions.add(new Condition(statement.address(), statement.value(), statement.operator()));
    }

    private record Statement(
            Operator operator,
            Address address,
            String value
    ) {
    }

    private static final class RuleBuilder {

        private final String id;
        private final List<Condition> conditions = new ArrayList<>();
        private final List<Action> actions = new ArrayList<>();

        private RuleBuilder(String id) {
            this.id = id;
        }

        private Rule build() {
            return new Rule(id, conditions, actions);
        }

    }

    private static final class FrameBuilder {

        private final String name;
        private final List<SlotDefinition> slots = new ArrayList<>();

        private FrameBuilder(String name) {
            this.name = name;
        }

    }

}
