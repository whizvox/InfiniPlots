package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.CommandDelegator;

import java.util.List;

public class InfiniPlotsCommandDelegator extends CommandDelegator {

  public InfiniPlotsCommandDelegator() {
    register("info", null, new InfoCommandHandler());
    register("worldinfo", List.of("winfo"), new WorldInfoCommandHandler());
    register("list", null, new ListCommandHandler());
    register("tp", List.of("tpplot"), new TeleportByWorldCommandHandler());
    register("tpowner", List.of("tpo"), new TeleportByOwnerCommandHandler());
    register("tpworld", List.of("tpw"), new TeleportToWorldCommandHandler());
    register("claim", null, new ClaimCommandHandler());
    register("claimhere", List.of("here"), new ClaimHereCommandHandler());
    register("claimfor", List.of("for"), new ClaimForCommandHandler());
    register("help", List.of("?"), new HelpCommandHandler());
    register("commands", List.of("cmds"), new ListCommandsCommandHandler(this));
    register("manual", List.of("man"), new ManualCommandHandler(this));
    register("genworld", null, new GenerateWorldCommandHandler());
    register("permissions", List.of("perms"), new PermissionsCommand());
    register("confirm", null, new ConfirmCommandHandler());
    register("deny", null, new DenyCommandHandler());
    register("unclaim", null, new UnclaimCommandHandler());
    register("unclaimfor", List.of("unfor"), new UnclaimForCommandHandler());
  }

}
