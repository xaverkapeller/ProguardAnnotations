package com.github.wrdlbrnft.proguardannotations.includestatements;

import javax.lang.model.element.Element;

/**
 * Created by Xaver on 09/04/16.
 */
public interface IncludeStatement {

    static IncludeStatement of(Element member) {
        return new IncludeStatementImpl(member);
    }
}
