package ru.nstu.nest.inference.source.kb;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class KlbFileReader {

    private static final Charset WINDOWS_1251 = Charset.forName("windows-1251");

    public String read(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return decode(bytes, StandardCharsets.UTF_8)
                    .orElseGet(() -> decode(bytes, WINDOWS_1251)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Не удалось распознать кодировку " + path.getFileName()
                            )));
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать " + path.getFileName(), e);
        }
    }

    private Optional<String> decode(byte[] bytes, Charset charset) {
        try {
            return Optional.of(charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString());
        } catch (CharacterCodingException e) {
            return Optional.empty();
        }
    }

}
