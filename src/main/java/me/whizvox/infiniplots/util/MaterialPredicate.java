package me.whizvox.infiniplots.util;

import me.whizvox.infiniplots.InfiniPlots;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public interface MaterialPredicate extends Predicate<Material> {

  MaterialPredicate CONTRADICTION = type -> false;

  static MaterialPredicate material(Material mat) {
    return type -> type == mat;
  }

  static MaterialPredicate materials(Collection<Material> mats) {
    return mats::contains;
  }

  static MaterialPredicate materials(Material... mats) {
    return materials(Set.of(mats));
  }

  static MaterialPredicate tag(Tag<Material> tag) {
    return tag::isTagged;
  }

  static MaterialPredicate tags(Collection<Tag<Material>> tags) {
    return type -> tags.stream().anyMatch(tag -> tag.isTagged(type));
  }

  @SafeVarargs
  static MaterialPredicate tags(Tag<Material>... tags) {
    return tags(List.of(tags));
  }

  static MaterialPredicate or(Predicate<Material> a, Predicate<Material> b) {
    return type -> a.test(type) || b.test(type);
  }

  @SafeVarargs
  static MaterialPredicate or(Predicate<Material>... predicates) {
    return type -> Arrays.stream(predicates).anyMatch(predicate -> predicate.test(type));
  }

  static MaterialPredicate composite(Collection<Material> mats, Collection<Tag<Material>> tags) {
    if (mats.isEmpty()) {
      if (tags.isEmpty()) {
        return CONTRADICTION;
      } else if (tags.size() == 1) {
        return tag(tags.stream().findFirst().get());
      } else {
        return tags(tags);
      }
    } else if (mats.size() == 1) {
      Material mat = mats.stream().findFirst().get();
      if (tags.isEmpty()) {
        return material(mat);
      } else if (tags.size() == 1) {
        return or(material(mat), tag(tags.stream().findFirst().get()));
      } else {
        return or(material(mat), tags(tags));
      }
    } else {
      if (tags.isEmpty()) {
        return materials(mats);
      } else if (tags.size() == 1) {
        return or(materials(mats), tag(tags.stream().findFirst().get()));
      } else {
        return or(materials(mats), tags(tags));
      }
    }
  }

  static MaterialPredicate composite(Object... objs) {
    List<Material> mats = new ArrayList<>();
    List<Tag<Material>> tags = new ArrayList<>();
    for (Object obj : objs) {
      if (obj instanceof Material mat) {
        mats.add(mat);
      } else if (obj instanceof Tag<?> tag) {
        try {
          //noinspection unchecked
          tags.add((Tag<Material>) tag);
        } catch (ClassCastException e) {
          InfiniPlots.getInstance().getLogger().log(Level.WARNING, "Expected material tag", e);
        }
      }
    }
    return composite(mats, tags);
  }

  static MaterialPredicate composite(Iterable<String> keys) {
    List<Material> mats = new ArrayList<>();
    List<Tag<Material>> tags = new ArrayList<>();
    keys.forEach(s -> {
      if (s.startsWith("#")) {
        String tagStr = s.substring(1);
        NamespacedKey tagKey = NamespacedKey.fromString(tagStr);
        if (tagKey != null) {
          Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class);
          if (tag != null) {
            tags.add(tag);
          } else {
            InfiniPlots.getInstance().getLogger().warning("Could not find tag: " + s);
          }
        } else {
          InfiniPlots.getInstance().getLogger().warning("Improperly-formatted tag: " + s);
        }
      } else {
        Material mat = Material.matchMaterial(s);
        if (mat != null) {
          mats.add(mat);
        } else {
          InfiniPlots.getInstance().getLogger().warning("Could not find material: " + s);
        }
      }
    });
    return composite(mats, tags);
  }

}
