package ru.nstu.nest.inference.context;

import ru.nstu.nest.inference.dto.Address;
import ru.nstu.nest.inference.dto.Fact;
import ru.nstu.nest.inference.dto.Derivation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorkingMemory {

    private final Map<String, List<Fact>> storage = new LinkedHashMap<>();

    public List<Fact> accept(List<Fact> facts) {
        return facts.stream()
                .map(this::addOrMerge)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<Fact> addOrMerge(Fact newFact) {
        List<Fact> hypotheses = storage.computeIfAbsent(newFact.address().key(), _ -> new ArrayList<>());
        for (int index = 0; index < hypotheses.size(); index++) {
            Fact hypothesis = hypotheses.get(index);
            if (!hypothesis.hypothesisKey().equals(newFact.hypothesisKey())) {
                continue;
            }

            hypotheses.set(index, merge(hypothesis, newFact));
            return Optional.empty();
        }

        hypotheses.add(newFact);
        return Optional.of(newFact);
    }

    public List<Fact> find(String address) {
        return find(Address.parse(address));
    }

    public List<Fact> find(Address address) {
        return List.copyOf(findStored(address));
    }

    public boolean has(String address) {
        return !findStored(Address.parse(address)).isEmpty();
    }

    public List<Fact> facts() {
        return storage.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    private Fact merge(Fact current, Fact incoming) {
        if (!current.hypothesisKey().equals(incoming.hypothesisKey())) {
            throw new IllegalArgumentException("Можно объединять только факты с одинаковым ключом гипотезы");
        }

        List<Derivation> mergedHistory = new ArrayList<>(
                current.derivationHistory().size() + incoming.derivationHistory().size()
        );
        mergedHistory.addAll(current.derivationHistory());
        mergedHistory.addAll(incoming.derivationHistory());

        return new Fact(
                current.address(),
                current.value(),
                List.copyOf(new LinkedHashSet<>(mergedHistory))
        );
    }

    private List<Fact> findStored(Address address) {
        return storage.getOrDefault(address.key(), List.of());
    }

}
