package me.whizvox.infiniplots.flag;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FlagsAdapter implements Flags {

  protected final Map<String, Flag> flags;

  protected FlagsAdapter(Map<String, Flag> flags) {
    this.flags = flags;
  }

  public FlagsAdapter(Iterable<Flag> flags) {
    Map<String, Flag> tempMap = new HashMap<>();
    flags.forEach(flag -> tempMap.put(flag.name(), flag));
    this.flags = Collections.unmodifiableMap(tempMap);
  }

  @Override
  public boolean isEmpty() {
    return flags.isEmpty();
  }

  @Override
  public boolean contains(String flag) {
    return flags.containsKey(flag);
  }

  @Override
  public FlagValue getValue(String flag) {
    return flags.getOrDefault(flag, Flag.DENY).value();
  }

  @NotNull
  @Override
  public Iterator<Flag> iterator() {
    return flags.values().iterator();
  }

  @Override
  public Spliterator<Flag> spliterator() {
    return Spliterators.spliterator(flags.values(), Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL);
  }

}
