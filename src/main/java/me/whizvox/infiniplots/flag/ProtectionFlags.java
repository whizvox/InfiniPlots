package me.whizvox.infiniplots.flag;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class ProtectionFlags implements Iterable<String> {

  private final Set<String> flags;

  public ProtectionFlags(Set<String> flags) {
    this.flags = flags;
  }

  public Stream<String> stream() {
    return flags.stream();
  }

  public boolean contains(String flag) {
    return flags.contains(flag);
  }

  public void add(String flag) {
    flags.add(flag);
  }

  public void remove(String flag) {
    flags.remove(flag);
  }

  @Override
  public Iterator<String> iterator() {
    return flags.iterator();
  }

}
