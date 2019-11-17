package me.axieum.mcmod.mdc.command;

import com.mojang.authlib.GameProfile;
import me.axieum.mcmod.mdc.DiscordClient;
import me.axieum.mcmod.mdc.util.StringUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;

public class MDCCommandSender extends FakePlayer
{
    private static final UUID MDC_UUID = UUID.fromString("3665cd17-b83f-43b3-848c-e4d305271340");

    private final boolean quiet;
    private final Member member;
    private final TextChannel channel;

    public MDCCommandSender(Member member, TextChannel channel, boolean quiet)
    {
        this(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD),
             new GameProfile(MDC_UUID, "@" + member.getEffectiveName()),
             member,
             channel,
             quiet);
    }

    public MDCCommandSender(ServerWorld world, GameProfile name, Member member, TextChannel channel, boolean quiet)
    {
        super(world, name);
        this.member = member;
        this.channel = channel;
        this.quiet = quiet;
    }

    @Override
    public void sendMessage(ITextComponent component)
    {
        if (quiet || component == null) return;

        final String message = StringUtils.mcToDiscord(component.getFormattedText());
        DiscordClient.getInstance().sendMessage(message, channel);
    }
}
