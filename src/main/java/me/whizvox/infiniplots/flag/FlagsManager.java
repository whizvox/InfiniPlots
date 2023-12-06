package me.whizvox.infiniplots.flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;

public class FlagsManager extends FlagsAdapter {

  private final Map<String, Flag> defaults;

  public FlagsManager() {
    super(new HashMap<>());
    defaults = new HashMap<>(DefaultFlags.ALL_FLAGS);
  }

  @Override
  public FlagValue getValue(String flag) {
    return contains(flag) ? super.getValue(flag) : defaults.getOrDefault(flag, Flag.DENY).value();
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
