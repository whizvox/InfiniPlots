package me.whizvox.infiniplots.flag;

import java.util.HashMap;
import java.util.Spliterator;
import java.util.Spliterators;

public class FlagsManager extends FlagsAdapter {

  public FlagsManager() {
    super(new HashMap<>());
  }

  public void setDefaults() {
    flags.putAll(DefaultFlags.ALL_FLAGS);
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
