package util

import groovy.transform.InheritConstructors
import org.ods.util.Project

@InheritConstructors
class NewMockProject extends Project {

    Map loadMetadata(String filename) {
        def result = [
                id          : "pltfmdev",
                name        : "Sock Shop",
                description : "A socks-selling e-commerce demo application.",
                services    : [
                        bitbucket: [
                                credentials: [
                                        id: "pltfmdev-cd-cd-user-with-password"
                                ]
                        ],
                        jira     : [
                                credentials: [
                                        id: "pltfmdev-cd-cd-user-with-password"
                                ]
                        ],
                        nexus    : [
                                repository: [
                                        name: "leva-documentation"
                                ]
                        ]
                ],
                repositories: [
                        [
                                id  : "demo-app-carts",
                                type: "ods-service",
                                data: [
                                        documents: [:]
                                ]
                        ],
                        [
                                id  : "demo-app-catalogue",
                                type: "ods",
                                data: [
                                        documents: [:]
                                ]
                        ],
                        [
                                id  : "demo-app-front-end",
                                type: "ods",
                                data: [
                                        documents: [:]
                                ]
                        ],
                        [
                                id  : "demo-app-test",
                                type: "ods-test",
                                data: [
                                        documents: [:]
                                ]
                        ]
                ]
        ]

        result.repositories.each { repo ->
            repo.data?.git = [
                    branch: "origin/master",
                    commit: UUID.randomUUID().toString().replaceAll("-", ""),
                    previousCommit: UUID.randomUUID().toString().replaceAll("-", ""),
                    previousSucessfulCommit: UUID.randomUUID().toString().replaceAll("-", ""),
                    url: "https://cd_user@somescm.com/scm/someproject/${repo.id}.git"
            ]
            repo.metadata = [
                    name: "Sock Shop: ${repo.id}",
                    description: "Some description for ${repo.id}",
                    supplier: "https://github.com/microservices-demo/",
                    version: "1.0"
            ]
        }

        return result
    }

}
