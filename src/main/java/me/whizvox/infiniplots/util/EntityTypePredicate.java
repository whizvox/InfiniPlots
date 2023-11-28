package me.whizvox.infiniplots.util;

import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public interface EntityTypePredicate extends Predicate<EntityType> {

  static EntityTypePredicate type(EntityType type) {
    return t -> t == type;
  }

  static EntityTypePredicate types(Collection<EntityType> types) {
    return types::contains;
  }

  static EntityTypePredicate types(EntityType... types) {
    return types(Set.of(types));
  }

}
