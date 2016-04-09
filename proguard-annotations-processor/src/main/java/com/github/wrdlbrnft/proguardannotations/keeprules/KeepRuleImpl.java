package com.github.wrdlbrnft.proguardannotations.keeprules;

import com.github.wrdlbrnft.proguardannotations.Utils;
import com.github.wrdlbrnft.proguardannotations.includestatements.IncludeStatement;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Xaver on 09/04/16.
 */
class KeepRuleImpl implements KeepRule {

    private final String mProguardRule;

    KeepRuleImpl(TypeElement element, List<IncludeStatement> includeStatement) {
        mProguardRule = includeStatement.stream()
                .map(IncludeStatement::toString)
                .collect(Collectors.joining(
                        "\n\t",
                        "-keep " + Utils.formatType(element) + " {\n\t",
                        "\n}"
                ));
    }

    @Override
    public String toString() {
        return mProguardRule;
    }
}
