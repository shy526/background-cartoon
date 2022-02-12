package com.github.shy526.obj;

import com.google.common.base.Objects;
import lombok.Data;

@Data
public class Chapter {
    private String id;
    private String title;
    private Integer total;
    private Integer index;

    @Override
    public String toString() {
        return title + "-" + total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chapter chapter = (Chapter) o;
        return Objects.equal(id, chapter.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
