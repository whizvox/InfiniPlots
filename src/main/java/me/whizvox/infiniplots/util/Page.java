package me.whizvox.infiniplots.util;

import java.util.Collections;
import java.util.List;

public record Page<T>(int page, int limit, int totalItems, int totalPages, List<T> items) {

  public Page(int page, int limit, int totalItems, int totalPages, List<T> items) {
    this.page = page;
    this.limit = limit;
    this.totalItems = totalItems;
    this.totalPages = totalPages;
    this.items = Collections.unmodifiableList(items);
  }

}
