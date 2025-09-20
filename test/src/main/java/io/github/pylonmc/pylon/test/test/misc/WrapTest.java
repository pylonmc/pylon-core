package io.github.pylonmc.pylon.test.test.misc;

import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapEncoder;
import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapRepresentation;
import io.github.pylonmc.pylon.core.util.PylonUtils;
import io.github.pylonmc.pylon.test.base.AsyncTest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WrapTest extends AsyncTest {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    protected void test() {
        checkWrap(
                "<green>Lorem ipsum dolor sit amet",
                """
                <green>Lorem\s
                <green>ipsum\s
                <green>dolor\s
                <green>sit amet
                """
        );
        checkWrap(
                "<yellow>Lorem <red>ipsum dolor sit amet",
                """
                <yellow>Lorem\s
                <red>ipsum\s
                <red>dolor\s
                <red>sit amet
                """
        );
    }

    private void checkWrap(String original, String expected) {
        Component originalComponent = mm.deserialize(original);
        Component expectedComponent = Component.join(
                JoinConfiguration.newlines(),
                Arrays.stream(expected.split("\\n")).map(mm::deserialize).toList()
        );
        Component wrapped = wrap(originalComponent);
        assertThat(mm.serialize(wrapped)).isEqualTo(mm.serialize(expectedComponent));
    }

    private Component wrap(Component component) {
        LineWrapRepresentation repr = LineWrapEncoder.encode(component);
        List<String> newLines = new ArrayList<>();
        for (String line : repr.lines()) {
            newLines.addAll(PylonUtils.wrapText(line, 8));
        }
        List<TextComponent> lines = new LineWrapRepresentation(newLines, repr.styles()).toComponentLines();
        return Component.join(JoinConfiguration.newlines(), lines);
    }
}
