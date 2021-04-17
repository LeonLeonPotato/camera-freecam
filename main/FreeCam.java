package me.leon.trinity.hacks.misc;

import com.mojang.authlib.GameProfile;
import me.leon.trinity.events.main.EventPacketSend;
import me.leon.trinity.events.main.EventSetOpaqueCube;
import me.leon.trinity.events.main.MoveEvent;
import me.leon.trinity.events.settings.EventBooleanToggle;
import me.leon.trinity.hacks.Category;
import me.leon.trinity.hacks.Module;
import me.leon.trinity.hacks.client.ClickGUI;
import me.leon.trinity.hacks.render.FreeLook;
import me.leon.trinity.managers.ModuleManager;
import me.leon.trinity.setting.settings.Mode;
import me.leon.trinity.setting.settings.Slider;
import me.leon.trinity.utils.entity.MotionUtils;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

public class FreeCam extends Module {
    private static final Slider speed = new Slider("Speed", 0, 10, 20, false);

    public FreeCam() {
        super("FreeCam", "Out-of-body experience", Category.MISC);
    }

    private EntityPlayerCamera other;

    @Override
    public void onDisable() {
        if(nullCheck()) return;
        if(other == null) return;
        mc.world.removeEntity(other);
        setRender(mc.player);
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (nullCheck()) {
            this.setEnabled(false);
            return;
        }

        if(mc.currentScreen != null) return;
        if(other == null) return;

        double cameraYaw = other.rotationYaw;
        double cameraPitch = other.rotationPitch;

        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 8.0F;

        double dx = Mouse.getDX() * f1 * 0.15D;
        double dy = Mouse.getDY() * f1 * 0.15D;

        cameraYaw = cameraYaw + dx;
        cameraPitch = cameraPitch - dy;

        cameraPitch = MathHelper.clamp(cameraPitch, -90.0F, 90.0F);

        other.rotationPitch = (float) cameraPitch;
        other.rotationYaw = (float) cameraYaw;

        float playerSpeed = (float) (speed.getValue() / 10);
        float moveForward = getMoveForward();
        float moveStrafe = getMoveStrafe();

        final double[] motions = MotionUtils.getMotion(playerSpeed, moveForward, moveStrafe, other);

        double x = motions[0];
        double z = motions[1];

        other.noClip = true;
        other.move(MoverType.PLAYER, x, getY(), z);

        other.inventory.copyInventory(mc.player.inventory);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        event.setCanceled(true);
    }

    @Override
    public void onEnable() {
        if(nullCheck()) return;

        this.other = new EntityPlayerCamera(mc.world, mc.player.gameProfile);
        other.copyLocationAndAnglesFrom(mc.player);
        other.inventory.copyInventory(mc.player.inventory);
        other.noClip = true;
        other.inventory.copyInventory(mc.player.inventory);
        other.setLocationAndAngles(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch);
        other.setRotationYawHead(mc.player.rotationYaw);
        mc.world.addEntityToWorld(-1234, other);
        setRender(other);
    }

    @EventHandler
    private final Listener<EventSetOpaqueCube> cubeListener = new Listener<>(event -> {
        event.cancel();
    });

    private float getMoveForward() {
        if(mc.gameSettings.keyBindForward.isKeyDown() && mc.gameSettings.keyBindBack.isKeyDown()) {
            return 0.0f;
        }
        if(mc.gameSettings.keyBindBack.isKeyDown()) {
            return -1.0f;
        }
        if(mc.gameSettings.keyBindForward.isKeyDown()) {
            return 1.0f;
        }
        return 0.0f;
    }

    private float getMoveStrafe() {
        if(mc.gameSettings.keyBindLeft.isKeyDown() && mc.gameSettings.keyBindRight.isKeyDown()) {
            return 0.0f;
        }
        if(mc.gameSettings.keyBindRight.isKeyDown()) {
            return -1.0f;
        }
        if(mc.gameSettings.keyBindLeft.isKeyDown()) {
            return 1.0f;
        }
        return 0.0f;
    }

    private double getY() {
        if(mc.gameSettings.keyBindSneak.isKeyDown() && mc.gameSettings.keyBindJump.isKeyDown()) {
            return 0.0f;
        }
        if(mc.gameSettings.keyBindJump.isKeyDown()) {
            return (speed.getValue() / 10);
        }
        if(mc.gameSettings.keyBindSneak.isKeyDown()) {
            return -(speed.getValue() / 10);
        }
        return 0.0;
    }

    private void setRender(Entity entity) {
        mc.renderViewEntity = entity;
        if (ModuleManager.getMod(ClickGUI.class).isEnabled()) {
            ClickGUI.loadShader();
        }
    }

    public boolean check() {
        return this.isEnabled() && !nullCheck();
    }

    private static class EntityPlayerCamera extends EntityOtherPlayerMP
    {
        public EntityPlayerCamera(World worldIn, GameProfile gameProfileIn)
        {
            super(worldIn, gameProfileIn);
        }

        @Override
        public boolean isInvisible()
        {
            return true;
        }

        @Override
        public boolean isInvisibleToPlayer(@NotNull EntityPlayer player)
        {
            return true;
        }

        @Override
        public boolean isSpectator()
        {
            return false;
        }
    }
}
