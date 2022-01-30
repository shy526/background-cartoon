package com.github.shy526.tool;

import com.github.shy526.service.StorageService;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * 持久化保存组件
 * 继承类需要配置注解
 *
 * @author shy526
 * @State(name = "自定义", storages = {@Storage("自定义.xml")})
 * plugin.xml 配置
 * <applicationService serviceImplementation="全限定类名"/>
 */
@Data
public abstract class AbsStorageService<T> implements PersistentStateComponent<T> {
    private Map<String, Object> storage;


    @Override
    public void loadState(@NotNull T state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    public <T> T getStorage(String key, Class<T> tClass) {
        Object obj = this.storage.get(key);
        if (tClass.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }

    public void setStorage(String key, Object value) {
        if (Objects.nonNull(value)) {
            storage.put(key, value);
        }
    }
}
