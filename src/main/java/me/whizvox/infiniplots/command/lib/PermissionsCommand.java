package me.whizvox.infiniplots.command.lib;

import me.whizvox.infiniplots.command.ArgumentHelper;
import me.whizvox.infiniplots.command.CommandContext;
import me.whizvox.infiniplots.command.CommandHandler;
import me.whizvox.infiniplots.command.SuggestionHelper;
import me.whizvox.infiniplots.exception.InterruptCommandException;
import me.whizvox.infiniplots.util.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PermissionsCommand extends CommandHandler {

  private static final int PAGE_SIZE = 7;

  private static final List<String> MANUAL = List.of(
      "List all permissions associated with this plugin.",
      "Examples:",
      "- &b/infiniplots permissions&r : List the first page of permissions",
      "- &b/infiniplots permissions 2&r : List the 2nd page of permissions"
  );

  private static String build(String basePermission, String description) {
    return "&binfiniplots." + basePermission + "&r : " + description;
  }

  private static final List<String> PERMISSIONS = List.of(
      build("bypass.<world>", "Allow a player to bypass building restrictions in a specific world"),
      build("claim", "Allow claiming plots (the &eclaim&r command)"),
      build("claim.tier.<ownerTier>", "Allow some maximum number of owned plots according to the &aplotOwnerTiers&r configuration"),
      build("claim.world.<world>", "Allow claiming plots in a specific world"),
      build("claim.inf", "Allow for an infinite number of owned plots"),
      build("claimfor", "Allow claiming for other players (the &eclaimfor&r command)"),
      build("claim.override", "Allow ignoring target permissions when using the &aoverride&r option for the &eclaimfor&r command"),
      build("commands", "Allow listing of plugin commands (the &ecommands&r command)"),
      build("genworld", "Allow generation of plot worlds (the &egenworld&r command)"),
      build("help", "Allow viewing of plugin help information (the &ehelp&r command)"),
      build("import", "Allow importing of worlds as plot worlds (the &eimport&r command)"),
      build("info", "Allow viewing of plot information (the &einfo&r command)"),
      build("list", "Allow usage of the &elist&r command"),
      build("list.plots.self", "Allow listing of plots belonging to yourself"),
      build("list.plots.other", "Allow listing of plots belonging to some other player"),
      build("list.worldplots", "Allow listing of plots in a world"),
      build("list.worlds", "Allow listing of plot worlds"),
      build("list.generators", "Allow listing of plot world generators"),
      build("lockdown", "Allow setting lockdown status of a plot world"),
      build("lockdown.bypass.<level>", "Allow a player to bypass the lockdown restrictions of a plot world"),
      build("manual", "Allow usage of the &emanual&r command"),
      build("manual.<command>", "Allow viewing manual of a specific command"),
      build("permissions", "Allow listing of plugin permissions (the &epermissions&r command)"),
      build("plotflag.list", "Allow listing of a plot's flags (the &eplotflag list&r command)"),
      build("plotflag.modify.<flag>", "Allow modification of a plot flag (the &eplotflag set|clear&r command)"),
      build("regen", "Allow regeneration of chunks (the &eregen&r command)"),
      build("reset", "Allow resetting of an owned plot (the &ereset&r command)"),
      build("tp.owner", "Allow teleporting to a plot via its owner (the &etpowner&r command)"),
      build("tp.world", "Allow teleporting to a plot via its world (the &etp&r command)"),
      build("tpworld", "Allow teleporting to other worlds (the &etpworld&r command)"),
      build("tpworld.<world>", "Allow teleporting to a specific world"),
      build("unclaim", "Allow a player to unclaim an owned plot (the &eunclaim&r command)"),
      build("unclaimfor", "Allow a player to unclaim another player's plot from them (the &eunclaimfor&e command)"),
      build("flag.list", "Allow listing of all protection flags (the &eflag list&r command)"),
      build("flag.test", "Allow testing of a flag (the &eflag test&r command)"),
      build("worldflag.get", "Allow retrieval of flag value (the &rworldflag get&r command)"),
      build("worldflag.modify.<flag>", "Allow modification of a world flag (the &eworldflag set|clear&r command)"),
      build("worldinfo", "Allow viewing of plot world information (the &eworldinfo&r command)"),
      build("worldinfo.<world>", "Allow viewing the information of a specific plot world")
  );

  @Override
  public List<String> listSuggestions(CommandContext context) {
    if (context.args().size() == 1) {
      return SuggestionHelper.pages(context.args().get(0), PERMISSIONS.size(), PAGE_SIZE);
    }
    return super.listSuggestions(context);
  }

  @Override
  public List<String> getManual() {
    return MANUAL;
  }

  @Override
  public boolean hasPermission(CommandSender sender) {
    return sender.hasPermission("infiniplots.permissions");
  }

  @Override
  public void execute(CommandContext context) throws InterruptCommandException {
    int page = ArgumentHelper.getInt(context, 0, () -> 1, 1, Integer.MAX_VALUE);
    List<String> message = new ArrayList<>();
    ChatUtils.listPages(PERMISSIONS, page, PAGE_SIZE, message);
    if (context.args().isEmpty() && PERMISSIONS.size() > PAGE_SIZE) {
      message.add("Run &b/infiniplots permissions 2&r to see the next page of permissions");
    }
    context.sendMessage(message);
  }

}
