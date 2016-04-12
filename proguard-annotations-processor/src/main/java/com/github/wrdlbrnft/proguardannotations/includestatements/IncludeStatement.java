package com.github.wrdlbrnft.proguardannotations.includestatements;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/**
 * Created by Xaver on 09/04/16.
 */
public interface IncludeStatement {

    String toProguardKeepStatement(ProcessingEnvironment processingEnv);

    static IncludeStatement of(Element member) {
        return new IncludeStatementImpl(member);
    }
}
