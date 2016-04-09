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

    KeepRuleImpl(Type type, TypeElement element, List<IncludeStatement> includeStatement) {
        mProguardRule = includeStatement.stream()
                .map(IncludeStatement::toString)
                .collect(Collectors.joining(
                        "\n\t",
                        getKeepInstructionForType(type) + " " + Utils.formatType(element) + " {\n\t",
                        "\n}"
                ));
    }

    private static String getKeepInstructionForType(Type type) {
        switch (type) {

            case KEEP_ALL:
                return "-keep";

            case KEEP_MEMBERS:
                return "-keepclassmembers";

            default:
                throw new IllegalStateException("Encountered unhandled KeepRule.Type: " + type);
        }
    }

    @Override
    public String toString() {
        return mProguardRule;
    }
}
