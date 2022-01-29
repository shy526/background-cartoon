package com.github.shy526.obj;

import com.google.common.base.Objects;
import lombok.Data;

import java.util.List;

/**
 * 漫画
 *
 * @author shy526
 */
@Data

public class Cartoon {
    private String cover;
    private String id;
    private String title;
    private List<Chapter> chapters;

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cartoon cartoon = (Cartoon) o;
        return Objects.equal(id, cartoon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
