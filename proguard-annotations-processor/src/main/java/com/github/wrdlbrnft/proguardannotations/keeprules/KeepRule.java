package com.github.wrdlbrnft.proguardannotations.keeprules;

import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xaver on 09/04/16.
 */
public interface KeepRule {

    enum Type {
        KEEP_ALL,
        KEEP_MEMBERS
    }

    class Builder {

        private final Type mType;
        private final TypeElement mElement;
        private final List<IncludeStatement> mStatements = new ArrayList<>();

        public Builder(Type type, TypeElement element) {
            mType = type;
            mElement = element;
        }

        public Builder add(IncludeStatement statement) {
            mStatements.add(statement);
            return this;
        }

        public KeepRule build() {
            return new KeepRuleImpl(mType, mElement, mStatements);
        }
    }
}
