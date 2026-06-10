package fr.dreamin.dreamHud.api.element;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.pack.font.FontLoaderService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Element that renders dynamic text using the configured HUD font.
 *
 * <p>Supports both plain text and pre-built Adventure Components (MiniMessage, player heads, etc.).</p>
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>{@code componentSupplier} takes priority over {@code textSupplier} when both are defined.</li>
 *   <li>Suppliers are invoked every time the HUD is rendered.</li>
 * </ul>
 *
 * <p><strong>Usage example (plain text)</strong>
 * <pre>{@code
 * TextElement timer = TextElement.builder()
 *     .textSupplier(() -> formatDuration(remainingTicks))
 *     .font("pixel")
 *     .build();
 * }</pre>
 *
 * <p><strong>Usage example (MiniMessage / player heads)</strong>
 * <pre>{@code
 * TextElement playerInfo = TextElement.builder()
 *     .componentSupplier(() -> MiniMessage.parse("<gold><head:" + player.getName() + "> " + player.getName()))
 *     .font("default")
 *     .build();
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter @Setter
@SuperBuilder
public final class TextElement extends Element {

  private final @NotNull CodexService codexService = DreamHud.getService(CodexService.class);

  private final @Nullable Supplier<String> textSupplier;
  private final @Nullable Supplier<Component> componentSupplier;

  // ###############################################################
  // ----------------------- ELEMENT METHODS -----------------------
  // ###############################################################

  @Override
  public @NotNull Component toComponent() {
    TextColor color = this.getColor();
    ShadowColor shadowColor = this.isShadow() ? this.getShadowColor() : ShadowColor.none();

    Key font = null;
    if (this.getFontString() != null)
      font = Key.key(this.codexService.getConfig().namespace, String.format("font_%s", this.getFontString()));
    else if (this.getFont() != null) font = this.getFont();

    Component content;
    if (this.componentSupplier != null) {
      content = this.componentSupplier.get();
    } else if (this.textSupplier != null) {
      content = Component.text(this.textSupplier.get());
    } else {
      content = Component.empty();
    }

    if (color != null) content = content.color(color);
    if (shadowColor != null) content = content.shadowColor(shadowColor);
    if (!getDecorations().isEmpty()) content = content.decorations(getDecorations());
    if (font != null) content = content.font(font);

    return content;
  }

  @Override
  public int getPixelWidth() {
    final var fontLoader = DreamHud.getService(FontLoaderService.class);
    final var fontName = getFontString() != null ? getFontString() : (getFont() != null ? getFont().value() : "default");

    if (this.componentSupplier != null) {
      return fontLoader.getComponentWidth(this.componentSupplier.get(), fontName);
    }

    if (this.textSupplier != null) {
      return fontLoader.getStringWidth(this.textSupplier.get(), fontName);
    }

    return 0;
  }
}
