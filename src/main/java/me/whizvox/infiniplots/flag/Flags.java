package me.whizvox.infiniplots.flag;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

public interface Flags extends Iterable<Flag> {

  boolean isEmpty();

  boolean contains(String flag);

  /**
   * Get the value of a flag.
   * @param flag The flag
   * @return The value of the flag, which should be {@link FlagValue#DENY} if the flag is not set
   */
  FlagValue getValue(String flag);

  Flags EMPTY = new Flags() {

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean contains(String flag) {
      return false;
    }

    @Override
    public FlagValue getValue(String flag) {
      return FlagValue.DENY;
    }

    @NotNull
    @Override
    public Iterator<Flag> iterator() {
      return Collections.emptyIterator();
    }

    @Override
    public Spliterator<Flag> spliterator() {
      return Spliterators.emptySpliterator();
    }

  };

}
