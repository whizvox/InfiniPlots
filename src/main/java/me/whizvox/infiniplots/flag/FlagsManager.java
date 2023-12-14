package me.whizvox.infiniplots.flag;

import me.whizvox.infiniplots.InfiniPlots;

import java.util.*;

public class FlagsManager extends FlagsAdapter {

  private final Map<String, FlagValue> defaults;

  public FlagsManager(Map<String, FlagValue> defaults) {
    super(new HashMap<>());
    this.defaults = Collections.unmodifiableMap(defaults);
  }

  public FlagsManager() {
    this(InfiniPlots.getInstance().getDefaultFlags());
  }

  @Override
  public FlagValue getValue(String flag) {
    return contains(flag) ? super.getValue(flag) : defaults.getOrDefault(flag, FlagValue.DENY);
  }

  public void set(Flag flag) {
    flags.put(flag.name(), flag);
  }

  public void set(Iterable<Flag> flags) {
    flags.forEach(this::set);
  }

  public void clear(String flag) {
    flags.remove(flag);
  }

  @Override
  public Spliterator<Flag> spliterator() {
    return Spliterators.spliterator(flags.values(), Spliterator.DISTINCT | Spliterator.NONNULL);
  }

}
