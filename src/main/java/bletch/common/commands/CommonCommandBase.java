package bletch.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.Collections;
import java.util.List;

public abstract class CommonCommandBase extends CommandBase {

    protected final String modId;
    protected final String prefix;
    protected final String name;

    public CommonCommandBase(String modId, String commandPrefix, String name) {
    	this.modId = modId;
    	this.prefix = commandPrefix != null && commandPrefix.endsWith(".") ? commandPrefix : commandPrefix + ".";
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList(this.name);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return this.prefix + this.name + ".usage";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        try {
            return PermissionAPI.hasPermission(getCommandSenderAsPlayer(sender), this.modId +"." + this.prefix + this.name);
        } catch (PlayerNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }
}
