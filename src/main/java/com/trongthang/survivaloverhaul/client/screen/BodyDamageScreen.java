package com.trongthang.survivaloverhaul.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyPart;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import com.trongthang.survivaloverhaul.networking.NetworkingConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import com.trongthang.survivaloverhaul.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BodyDamageScreen extends Screen {

    // Background screen texture
    private static final Identifier BG_TEXTURE = new Identifier("survivaloverhaul",
            "textures/gui/body_health_screen.png");
    // Body part limb sprite sheet (normal + highlight on hover)
    private static final Identifier BODY_PARTS_TEXTURE = new Identifier("survivaloverhaul",
            "textures/gui/body_parts_screen.png");

    // Background size
    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 183;

    // Health bar strip (inside the 256+ width of body_health_screen.png)
    private static final int HEALTH_BAR_WIDTH = 30;
    private static final int HEALTH_BAR_HEIGHT = 5;
    private static final int TEX_HEALTH_BAR_X = 176;
    private static final int TEX_HEALTH_BAR_Y = 0;

    // In body_parts_screen.png, highlighted version is offset 128 pixels to the
    // right
    private static final int HIGHLIGHT_OFFSET_X = 128;

    private final boolean isBandageMode;

    private int leftPos;
    private int topPos;

    public BodyDamageScreen(boolean isBandageMode) {
        super(Text.translatable("screen.survivaloverhaul.body_damage"));
        this.isBandageMode = isBandageMode;
    }

    @Override
    protected void init() {
        super.init();
        if (!ModConfig.enableBodyDamage) {
            this.close();
            return;
        }
        leftPos = (this.width - BG_WIDTH) / 2;
        topPos = (this.height - BG_HEIGHT) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Draw background panel
        context.drawTexture(BG_TEXTURE, leftPos, topPos, 0, 0, BG_WIDTH, BG_HEIGHT);

        if (isBandageMode) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Click a limb to heal it (uses 1 Bandage)"),
                    this.width / 2, topPos - 15, 0x00FF00);
        }

        // Draw limb buttons with hover highlight + wound state
        drawLimb(context, mouseX, mouseY, BodyPart.HEAD, leftPos + 68, topPos + 46, 38, 34);
        drawLimb(context, mouseX, mouseY, BodyPart.TORSO, leftPos + 73, topPos + 79, 28, 38);
        drawLimb(context, mouseX, mouseY, BodyPart.LEFT_ARM, leftPos + 23, topPos + 79, 50, 38);
        drawLimb(context, mouseX, mouseY, BodyPart.RIGHT_ARM, leftPos + 101, topPos + 79, 50, 38);
        drawLimb(context, mouseX, mouseY, BodyPart.LEFT_LEG, leftPos + 38, topPos + 117, 49, 36);
        drawLimb(context, mouseX, mouseY, BodyPart.RIGHT_LEG, leftPos + 87, topPos + 117, 49, 36);
        drawLimb(context, mouseX, mouseY, BodyPart.LEFT_FOOT, leftPos + 38, topPos + 153, 49, 10);
        drawLimb(context, mouseX, mouseY, BodyPart.RIGHT_FOOT, leftPos + 87, topPos + 153, 49, 10);

        // Draw health bars on top
        drawHealthBar(context, leftPos + 72, topPos + 46, BodyPart.HEAD);
        drawHealthBar(context, leftPos + 72, topPos + 92, BodyPart.TORSO);
        drawHealthBar(context, leftPos + 27, topPos + 80, BodyPart.LEFT_ARM);
        drawHealthBar(context, leftPos + 118, topPos + 80, BodyPart.RIGHT_ARM);
        drawHealthBar(context, leftPos + 39, topPos + 134, BodyPart.LEFT_LEG);
        drawHealthBar(context, leftPos + 106, topPos + 134, BodyPart.RIGHT_LEG);

        // Foot health bars
        drawHealthBar(context, leftPos + 39, topPos + 156, BodyPart.LEFT_FOOT);
        drawHealthBar(context, leftPos + 106, topPos + 156, BodyPart.RIGHT_FOOT);

        // Debugging logs to console if needed (remove after verified)
        // System.out.println("Left Foot Health: " +
        // ClientBodyDamageManager.getHealth(BodyPart.LEFT_FOOT));

        // Tooltips are drawn last so they're on top
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.HEAD, leftPos + 68, topPos + 46, 38, 34);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.TORSO, leftPos + 73, topPos + 79, 28, 38);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.LEFT_ARM, leftPos + 23, topPos + 79, 50, 38);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.RIGHT_ARM, leftPos + 101, topPos + 79, 50, 38);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.LEFT_LEG, leftPos + 38, topPos + 117, 49, 36);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.RIGHT_LEG, leftPos + 87, topPos + 117, 49, 36);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.LEFT_FOOT, leftPos + 38, topPos + 153, 49, 10);
        maybeDrawTooltip(context, mouseX, mouseY, BodyPart.RIGHT_FOOT, leftPos + 87, topPos + 153, 49, 10);

        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Draw a limb's sprite using body_parts_screen.png.
     * Normal limb sprites are in the left half; highlighted are +128 px to the
     * right.
     * Row 0 = Healthy, Row 1 = Wounded, Row 2 = Dead (indexY * icon height).
     */
    private void drawLimb(DrawContext context, int mouseX, int mouseY,
            BodyPart part, int bx, int by, int bw, int bh) {
        LimbIcon icon = LimbIcon.get(part);
        if (icon == null)
            return;

        IBodyDamageData data = (IBodyDamageData) this.client.player;
        float health = data.survivalOverhaul$getBodyDamageManager().getHealth(part);
        float ratio = health / part.getMaxHealth();
        int conditionRow = LimbCondition.get(ratio).iconIndexY;

        boolean hovering = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        int texOffsetX = hovering ? HIGHLIGHT_OFFSET_X : 0;

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.drawTexture(BODY_PARTS_TEXTURE,
                bx + icon.posInButtonX,
                by + icon.posInButtonY,
                icon.texX + texOffsetX,
                icon.texY + conditionRow * icon.height,
                icon.width, icon.height);
    }

    /**
     * Draw a limb health bar strip from body_health_screen.png.
     * Condition Y index maps to: 0=Healthy, 1=Wounded, 2=Heavily Wounded, 3=Dead.
     */
    private void drawHealthBar(DrawContext context, int x, int y, BodyPart part) {
        IBodyDamageData data = (IBodyDamageData) this.client.player;
        float health = data.survivalOverhaul$getBodyDamageManager().getHealth(part);
        float ratio = health / part.getMaxHealth();

        // Condition row in the health bar strip
        int conditionRow;
        if (ratio <= 0.0f)
            conditionRow = 3;
        else if (ratio < 0.33f)
            conditionRow = 2;
        else if (ratio < 0.66f)
            conditionRow = 1;
        else
            conditionRow = 0;

        // Draw empty bar background (Dead strip)
        context.drawTexture(BG_TEXTURE, x, y,
                TEX_HEALTH_BAR_X, TEX_HEALTH_BAR_Y + HEALTH_BAR_HEIGHT * 3,
                HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

        int filledWidth = (int) Math.ceil(HEALTH_BAR_WIDTH * ratio);
        if (filledWidth > 0) {
            context.drawTexture(BG_TEXTURE, x, y,
                    TEX_HEALTH_BAR_X, TEX_HEALTH_BAR_Y + HEALTH_BAR_HEIGHT * conditionRow,
                    filledWidth, HEALTH_BAR_HEIGHT);
        }
    }

    private void maybeDrawTooltip(DrawContext context, int mouseX, int mouseY,
            BodyPart part, int x, int y, int w, int h) {
        if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
            IBodyDamageData data = (IBodyDamageData) this.client.player;
            float health = data.survivalOverhaul$getBodyDamageManager().getHealth(part);
            context.drawTooltip(this.textRenderer,
                    Text.literal(part.getDisplayName() + ": " +
                            String.format("%.1f", health) + " / " +
                            String.format("%.1f", part.getMaxHealth())),
                    mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isBandageMode) {
            BodyPart clicked = getClickedPart((int) mouseX, (int) mouseY);
            if (clicked != null) {
                IBodyDamageData data = (IBodyDamageData) this.client.player;
                if (data.survivalOverhaul$getBodyDamageManager().getHealth(clicked) < clicked.getMaxHealth()) {
                    sendHealLimbPacket(clicked);

                    this.close(); // 1 heal per bandage, then close and consume
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private BodyPart getClickedPart(int mx, int my) {
        if (isInside(mx, my, leftPos + 68, topPos + 46, 38, 34))
            return BodyPart.HEAD;
        if (isInside(mx, my, leftPos + 73, topPos + 79, 28, 38))
            return BodyPart.TORSO;
        if (isInside(mx, my, leftPos + 23, topPos + 79, 50, 38))
            return BodyPart.LEFT_ARM;
        if (isInside(mx, my, leftPos + 101, topPos + 79, 50, 38))
            return BodyPart.RIGHT_ARM;
        if (isInside(mx, my, leftPos + 38, topPos + 117, 49, 36))
            return BodyPart.LEFT_LEG;
        if (isInside(mx, my, leftPos + 87, topPos + 117, 49, 36))
            return BodyPart.RIGHT_LEG;
        if (isInside(mx, my, leftPos + 38, topPos + 153, 49, 10))
            return BodyPart.LEFT_FOOT;
        if (isInside(mx, my, leftPos + 87, topPos + 153, 49, 10))
            return BodyPart.RIGHT_FOOT;
        return null;
    }

    private boolean isInside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void sendHealLimbPacket(BodyPart part) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(part.name());
        ClientPlayNetworking.send(NetworkingConstants.HEAL_LIMB_C2S_ID, buf);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // -------------------------------------------------------------------------
    // Limb sprite layout, matching BodyPartButton.BodyPartIcon from LSO
    // -------------------------------------------------------------------------
    private enum LimbCondition {
        HEALTHY(0),
        WOUNDED(1),
        DEAD(2);

        public final int iconIndexY;

        LimbCondition(int iconIndexY) {
            this.iconIndexY = iconIndexY;
        }

        public static LimbCondition get(float ratio) {
            if (ratio <= 0f)
                return DEAD;
            if (ratio < 0.66f)
                return WOUNDED;
            return HEALTHY;
        }
    }

    /**
     * Maps our 8 BodyPart values to the closest icon in body_parts_screen.png.
     * Fields: posInButtonX, posInButtonY, texX, texY, width, height
     * (posInButton is the pixel offset of the sprite inside the hitbox rect)
     */
    private enum LimbIcon {
        HEAD(5, 11, 0, 0, 28, 26),
        TORSO(0, 4, 0, 80, 28, 34), // CHEST icon
        LEFT_ARM(12, 4, 28, 0, 38, 34),
        RIGHT_ARM(0, 4, 67, 0, 38, 34),
        LEFT_LEG(34, 0, 31, 107, 16, 36),
        RIGHT_LEG(0, 0, 47, 107, 16, 36),
        LEFT_FOOT(34, 0, 63, 107, 14, 10),
        RIGHT_FOOT(1, 0, 77, 107, 14, 10);

        public final int posInButtonX;
        public final int posInButtonY;
        public final int texX;
        public final int texY;
        public final int width;
        public final int height;

        LimbIcon(int px, int py, int tx, int ty, int w, int h) {
            this.posInButtonX = px;
            this.posInButtonY = py;
            this.texX = tx;
            this.texY = ty;
            this.width = w;
            this.height = h;
        }

        public static LimbIcon get(BodyPart part) {
            return switch (part) {
                case HEAD -> HEAD;
                case TORSO -> TORSO;
                case LEFT_ARM -> LEFT_ARM;
                case RIGHT_ARM -> RIGHT_ARM;
                case LEFT_LEG -> LEFT_LEG;
                case RIGHT_LEG -> RIGHT_LEG;
                case LEFT_FOOT -> LEFT_FOOT;
                case RIGHT_FOOT -> RIGHT_FOOT;
            };
        }
    }
}
