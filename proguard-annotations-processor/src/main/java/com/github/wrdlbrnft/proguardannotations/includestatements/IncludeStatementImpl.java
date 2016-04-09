package com.github.wrdlbrnft.proguardannotations.includestatements;

import com.github.wrdlbrnft.proguardannotations.Utils;

import javax.lang.model.element.Element;

/**
 * Created by Xaver on 09/04/16.
 */
class IncludeStatementImpl implements IncludeStatement {

    private final String mStatement;

    IncludeStatementImpl(Element element) {
        mStatement = Utils.formatMember(element);
    }

    @Override
    public String toString() {
        return mStatement;
    }
}
