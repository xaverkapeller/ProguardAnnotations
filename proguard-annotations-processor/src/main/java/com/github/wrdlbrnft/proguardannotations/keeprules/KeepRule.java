package com.github.wrdlbrnft.proguardannotations.keeprules;

import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xaver on 09/04/16.
 */
public interface KeepRule {

    class Builder {

        private final TypeElement mElement;
        private final List<IncludeStatement> mStatements = new ArrayList<>();

        public Builder(TypeElement element) {
            mElement = element;
        }

        public Builder add(IncludeStatement statement) {
            mStatements.add(statement);
            return this;
        }

        public KeepRule build() {
            return new KeepRuleImpl(mElement, mStatements);
        }
    }
}
