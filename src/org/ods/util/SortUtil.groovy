package org.ods.util

import com.cloudbees.groovy.cps.NonCPS

class SortUtil {

    @NonCPS
    // Sorts a collection of maps in the order of the keys in properties
    static List<Map> sortIssuesByProperties(Collection<Map> issues, List keys) {
        return issues.sort { issue ->
            issue.subMap(keys).values().collect { value ->
                // we use zero padding in Jira key number to allow sorting of Strings in numeric order
                value.split("-").last().padLeft(12,"0")
            }.join("-")
        }
    }
}
