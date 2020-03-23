package org.ods.util

import com.cloudbees.groovy.cps.NonCPS

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

import java.nio.file.Paths

import org.apache.http.client.utils.URIBuilder
import org.ods.service.JiraService
import org.ods.usecase.*
import org.yaml.snakeyaml.Yaml

class Project {

    class JiraDataItem implements Map, Serializable {
        static final String TYPE_BUGS = "bugs"
        static final String TYPE_COMPONENTS = "components"
        static final String TYPE_EPICS = "epics"
        static final String TYPE_MITIGATIONS = "mitigations"
        static final String TYPE_REQUIREMENTS = "requirements"
        static final String TYPE_RISKS = "risks"
        static final String TYPE_TECHSPECS = "techSpecs"
        static final String TYPE_TESTS = "tests"
        static final String TYPE_DOCS = "docs"

        static final List TYPES = [
            TYPE_BUGS,
            TYPE_COMPONENTS,
            TYPE_EPICS,
            TYPE_MITIGATIONS,
            TYPE_REQUIREMENTS,
            TYPE_RISKS,
            TYPE_TECHSPECS,
            TYPE_TESTS
        ]

        private final String type
        private HashMap delegate

        JiraDataItem(Map map, String type) {
            this.delegate = new HashMap(map)
            this.type = type
        }

        @NonCPS
        @Override
        int size() {
            return delegate.size()
        }

        @NonCPS
        @Override
        boolean isEmpty() {
            return delegate.isEmpty()
        }

        @NonCPS
        @Override
        boolean containsKey(Object key) {
            return delegate.containsKey(key)
        }

        @NonCPS
        @Override
        boolean containsValue(Object value) {
            return delegate.containsValue(value)
        }

        @NonCPS
        @Override
        Object get(Object key) {
            return delegate.get(key)
        }

        @NonCPS
        @Override
        Object put(Object key, Object value) {
            return delegate.put(key, value)
        }

        @NonCPS
        @Override
        Object remove(Object key) {
            return delegate.remove(key)
        }

        @NonCPS
        @Override
        void putAll(Map m) {
            delegate.putAll(m)
        }

        @NonCPS
        @Override
        void clear() {
            delegate.clear()
        }

        @NonCPS
        @Override
        Set keySet() {
            return delegate.keySet()
        }

        @NonCPS
        @Override
        Collection values() {
            return delegate.values()
        }

        @NonCPS
        @Override
        Set<Entry> entrySet() {
            return delegate.entrySet()
        }

        @NonCPS
        String getType() {
            return type
        }

        @NonCPS
        Map getDelegate() {
            return delegate
        }

        @NonCPS
        JiraDataItem cloneIt() {
            def bos = new ByteArrayOutputStream()
            def os = new ObjectOutputStream(bos)
            os.writeObject(this.delegate)
            def ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))

            def newDelegate = ois.readObject()
            JiraDataItem result = new JiraDataItem(newDelegate, type)
            return result
        }

        @NonCPS
        // FIXME: why can we not invoke derived methods in short form, e.g. .resolvedBugs?
        private List<JiraDataItem> getResolvedReferences(String type) {
            // Reference this within jiraResolved (contains readily resolved references to other entities)
            def item = Project.this.data.jiraResolved[this.type][this.key]
            return item[type] ?: []
        }

        List<JiraDataItem> getResolvedBugs() {
            return this.getResolvedReferences("bugs")
        }

        List<JiraDataItem> getResolvedComponents() {
            return this.getResolvedReferences("components")
        }

        List<JiraDataItem> getResolvedEpics() {
            return this.getResolvedReferences("epics")
        }

        List<JiraDataItem> getResolvedMitigations() {
            return this.getResolvedReferences("mitigations")
        }

        List<JiraDataItem> getResolvedSystemRequirements() {
            return this.getResolvedReferences("requirements")
        }

        List<JiraDataItem> getResolvedRisks() {
            return this.getResolvedReferences("risks")
        }

        List<JiraDataItem> getResolvedTechnicalSpecifications() {
            return this.getResolvedReferences("techSpecs")
        }

        List<JiraDataItem> getResolvedTests() {
            return this.getResolvedReferences("tests")
        }
    }

    class TestType {
        static final String ACCEPTANCE = "Acceptance"
        static final String INSTALLATION = "Installation"
        static final String INTEGRATION = "Integration"
        static final String UNIT = "Unit"
    }

    class GampTopic {
        static final String AVAILABILITY_REQUIREMENT = "Availability Requirement"
        static final String CONSTRAINT = "Constraint"
        static final String FUNCTIONAL_REQUIREMENT = "Functional Requirement"
        static final String INTERFACE_REQUIREMENT = "Interface Requirement"
    }

    protected static String METADATA_FILE_NAME = "metadata.yml"

    private static final TEMP_FAKE_JIRA_DATA = """
{
    "project": {
        "name": "Sock Shop",
        "description": "Sock Shop: A Microservice Demo Application",
        "key": "SOCKSHOP",
        "id": "1",
        "jiraBaseUrl": "https://jira.example.com:2990/jira/SOCKSHOP",
        "gampTopics": [
            "operational requirements",
            "functional requirements",
            "data requirements",
            "technical requirements",
            "interface requirements",
            "environment requirements",
            "performance requirements",
            "availability requirements",
            "security requirements",
            "maintenance requirements",
            "regulatory requirements",
            "roles",
            "compatibility",
            "procedural constraints",
            "overarching requirements"
        ],
        "projectProperties": {
            "PROJECT.POO_CAT.HIGH": "Frequency of the usage of the related function is >10 times per week.",
            "PROJECT.POO_CAT.LOW": "Frequency of the usage of the related function is <10 times per year.",
            "PROJECT.POO_CAT.MEDIUM": "Frequency of the usage of the related function is <10 times per week.",
            "PROJECT.USES_POO": "true"
        },
        "enumDictionary": {
            "ProbabilityOfDetection": {
                "1": {
                    "value": 1,
                    "text": "Immediate",
                    "short": "I"
                },
                "2": {
                    "value": 2,
                    "text": "Before Impact",
                    "short": "B"
                },
                "3": {
                    "value": 3,
                    "text": "After Impact",
                    "short": "A"
                }
            },
            "SeverityOfImpact": {
                "1": {
                    "value": 1,
                    "text": "Low",
                    "short": "L"
                },
                "2": {
                    "value": 2,
                    "text": "Medium",
                    "short": "M"
                },
                "3": {
                    "value": 3,
                    "text": "High",
                    "short": "H"
                }
            },
            "ProbabilityOfOccurrence": {
                "1": {
                    "value": 1,
                    "text": "LOW",
                    "short": "L"
                },
                "2": {
                    "value": 2,
                    "text": "MEDIUM",
                    "short": "M"
                },
                "3": {
                    "value": 3,
                    "text": "HIGH",
                    "short": "H"
                }
            },
            "RiskPriority": {
                "0": {
                    "value": 0,
                    "text": "N/A",
                    "short": "N"
                },
                "1": {
                    "value": 1,
                    "text": "HIGH",
                    "short": "H"
                },
                "2": {
                    "value": 2,
                    "text": "MEDIUM",
                    "short": "M"
                },
                "3": {
                    "value": 3,
                    "text": "LOW",
                    "short": "L"
                }
            },
            "GxPRelevance": {
                "R2": {
                    "value": 2,
                    "text": "Relevant",
                    "short": "R2"
                },
                "N0": {
                    "value": 0,
                    "text": "Not relevant/ZERO",
                    "short": "N0"
                },
                "N1": {
                    "value": 1,
                    "text": "Not relevant/LESS",
                    "short": "N1"
                },
                "N2": {
                    "value": 2,
                    "text": "Not relevant/EQUAL",
                    "short": "N2"
                }
            }
        }
    },
    "components": {
        "Technology-demo-app-payment": {
            "key": "Technology-demo-app-payment",
            "id": "2",
            "version": "1.0",
            "name": "Technology-demo-app-payment",
            "description": "Technology component demo-app-paymnent stored at https://bitbucket-dev.biscrum.com/projects/PLTFMDEV/repos/pltfmdev-demo-app-payment/browse",
            "epics": [
                "NET-124"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "tests": [
                "NET-127"
            ],
            "risks": [
                "NET-126"
            ],
            "mitigations": [
                "NET-123"
            ]
        }
    },
    "epics": {
        "NET-124": {
            "key": "NET-124",
            "id": "124",
            "version": "1.0",
            "name": "As a user I want to be able to do payments",
            "description": "Implement a payment service that integrates with Payment Service Providers and allows a secure and reliable payment for merchandise offered in the Sock Shop.",
            "status": "Open",
            "epicName": "Payments",
            "requirements": [
                "NET-125"
            ]
        }
    },
    "mitigations": {
        "NET-123": {
            "key": "NET-123",
            "id": "123",
            "version": "1.0",
            "name": "Provide discount for the next purchase",
            "description": "If a payment cannot be processed within the required time provide a discount for the next purchase for the same customer to make them come back.",
            "status": "TO DO",
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "risks": [
                "NET-126"
            ]
        }
    },
    "requirements": {
        "NET-125": {
            "key": "NET-125",
            "id": "125",
            "version": "1.0",
            "name": "As a user I want my payments to be processed quickly",
            "description": "Payments have to be conducted quickly to keep up with the elevated expectations of customers",
            "status": "IN DESIGN",
            "gampTopic": "performance requirements",
            "acceptanceCriteria": "acceptance of Req-1 only if ...",
            "configSpec": {
                "name": "Payment Service must be configured to communicate with payment service providers",
                "description": "Configuration for secure and reliable payments."
            },
            "funcSpec": {
                "name": "The payment must be confirmed in less than a set interval",
                "description": "A payment must be completed with the Payment Service Provider within the given time interval",
                "acceptanceCriteria": "The desired payment interval can be configured on system level."
            },
            "components": [
                "Technology-demo-app-payment"
            ],
            "epics": [
                "NET-124"
            ],
            "risks": [
                "NET-126"
            ],
            "tests": [
                "NET-127"
            ],
            "mitigations": [
                "NET-123"
            ],
            "techSpecs": [
                "NET-128"
            ]
        }
    },
    "risks": {
        "NET-126": {
            "key": "NET-126",
            "id": "126",
            "version": "1.0",
            "name": "If payments take too long we can loose business and customers",
            "description": "Adverse Event: Payments take too long. Impact: User will take their business elsewhere.",
            "status": "TO DO",
            "gxpRelevance": "N0",
            "probabilityOfOccurrence": 1,
            "severityOfImpact": 3,
            "probabilityOfDetection": 2,
            "riskPriorityNumber": 0,
            "riskPriority": 0,
            "mitigations": [
                "NET-123"
            ],
            "requirements": [
                "NET-125"
            ],
            "tests": [
                "NET-127"
            ]
        }
    },
    "techSpecs": {
        "NET-128": {
            "key": "NET-128",
            "id": "128",
            "version": "1.0",
            "name": "Containerized Infrastructure",
            "description": "The system should be set up as containerized infrastructure in the openshift cluster.",
            "status": "IN DESIGN",
            "systemDesignSpec": "Use containerized infrastructure to support quick and easy provisioning of a multitude of micro services that do one thing only and one thing right and fast.",
            "softwareDesignSpec": "Implement the system using a loosely coupled micro services architecture for improved extensibility and maintainability.",
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "risks": [
                "NET-126"
            ],
            "tests": [
                "NET-5",
                "NET-9",
                "NET-10",
                "NET-127",
                "NET-130",
                "NET-131",
                "NET-132",
                "NET-133",
                "NET-134",
                "NET-135",
                "NET-136",
                "NET-137",
                "NET-138",
                "NET-139",
                "NET-140",
                "NET-141",
                "NET-142",
                "NET-143",
                "NET-144"
            ]
        }
    },
    "tests": {
        "NET-127": {
            "key": "NET-127",
            "id": "127",
            "version": "1.0",
            "name": "Stress test for the payment duration SLAs",
            "description": "verify payments are executed within the payment SLAs",
            "status": "DONE",
            "testType": "Acceptance",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/health via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns 'OK'"
                },
                {
                    "index": 0,
                    "step": "Connect to the service on :80/health via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns 'OK'"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-130": {
            "name": "Verify database is correctly installed",
            "description": "Verify database is correctly setup.",
            "key": "NET-130",
            "id": "130",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Installation",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to database",
                    "data": "database credentials",
                    "expectedResult": "Connection to database is available and user is authenticated"
                },
                {
                    "index": 1,
                    "step": "List and verify databases",
                    "data": "database credentials; Sock Shop DB",
                    "expectedResult": "authenticated user sees all required databases"
                },
                {
                    "index": 2,
                    "step": "Use Sock Shop database",
                    "data": "SockShopDB",
                    "expectedResult": "Authenticated user can switch to Sock Shop DB and see tables"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "risks": [
                "DEMO-50"
            ],
            "bugs": []
        },
        "NET-131": {
            "name": "User interacts with the cart",
            "description": "User interacts with the cart",
            "key": "NET-131",
            "id": "131",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Acceptance",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "User logs into web shop",
                    "data": "N/A",
                    "expectedResult": "Webshop Landing Page gets displayed"
                },
                {
                    "index": 1,
                    "step": "User adds item to shopping cart",
                    "data": "N/A",
                    "expectedResult": "One item added to shopping cart"
                },
                {
                    "index": 2,
                    "step": "User follows link to shopping cart",
                    "data": "N/A",
                    "expectedResult": "Shopping cart is displayed, containing one item."
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-132": {
            "name": "User shows catalogue",
            "description": "User shows catalogue",
            "key": "NET-132",
            "id": "132",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Acceptance",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "User logs into web shop",
                    "data": "N/A",
                    "expectedResult": "Webshop Landing Page gets displayed"
                },
                {
                    "index": 1,
                    "step": "User follows link to catalogue",
                    "data": "N/A",
                    "expectedResult": "Catalogue is displayed in web page."
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-133": {
            "name": "User buys some socks",
            "description": "User buys some socks",
            "key": "NET-133",
            "id": "133",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Acceptance",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "User logs into web shop",
                    "data": "N/A",
                    "expectedResult": "Webshop Landing Page gets displayed"
                },
                {
                    "index": 1,
                    "step": "User adds item to shopping cart",
                    "data": "N/A",
                    "expectedResult": "One item added to shopping cart"
                },
                {
                    "index": 2,
                    "step": "User follows link to shopping cart",
                    "data": "N/A",
                    "expectedResult": "Shopping cart is displayed, containing one item."
                },
                {
                    "index": 3,
                    "step": "User clicks 'buy now' button",
                    "data": "N/A",
                    "expectedResult": "Shipping details are displayed."
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-134": {
            "name": "Home page looks sexy",
            "description": "Home page looks sexy",
            "key": "NET-134",
            "id": "134",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Acceptance",
            "executionType": "Automated",
            "steps": [],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-135": {
            "name": "User logs in",
            "description": "User logs in",
            "key": "NET-135",
            "id": "135",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Acceptance",
            "executionType": "Automated",
            "steps": [],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-136": {
            "name": "User exists in system",
            "description": "User exists in system",
            "key": "NET-136",
            "id": "136",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Integration",
            "executionType": "Automated",
            "steps": [],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-137": {
            "name": "FirstResultOrDefault returns the default for an empty list",
            "description": "FirstResultOrDefault returns the default for an empty list",
            "key": "NET-137",
            "id": "137",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Unit",
            "executionType": "Automated",
            "steps": [],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "DEMO-15"
            ],
            "bugs": []
        },
        "NET-138": {
            "name": "Verify frontend is correctly installed",
            "description": "Verify frontend is correctly installed.",
            "key": "NET-138",
            "id": "138",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Installation",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/health via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns 'OK'"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "risks": [
                "DEMO-50"
            ],
            "bugs": []
        },
        "NET-139": {
            "name": "Verify payment service is correctly installed",
            "description": "Verify payment service is correctly setup.",
            "key": "NET-139",
            "id": "139",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Installation",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/health via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns 'OK'"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-140": {
            "name": "Verify order service is correctly installed",
            "description": "Verify order service is correctly installed.",
            "key": "NET-140",
            "id": "140",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Installation",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/health via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns 'OK'"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-141": {
            "name": "Verify shipping service is correctly installed",
            "description": "Verify shipping service is correctly installed.",
            "key": "NET-141",
            "id": "141",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Installation",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/health via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns 'OK'"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-142": {
            "name": "Cart gets processed correctly",
            "description": "Cart gets processed correctly.",
            "key": "NET-142",
            "id": "142",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Integration",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/carts via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns correct cart data"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-143": {
            "name": "Frontend retrieves cart data correctly",
            "description": "Frontend retrieves cart data correctly.",
            "key": "NET-143",
            "id": "143",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Integration",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/carts via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns correct cart data"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        },
        "NET-144": {
            "name": "Frontend retrieves payment data correctly",
            "description": "Frontend retrieves payment data correctly.",
            "key": "NET-144",
            "id": "144",
            "version": "1.0",
            "status": "READY TO TEST",
            "testType": "Integration",
            "executionType": "Automated",
            "steps": [
                {
                    "index": 0,
                    "step": "Connect to the service on :80/payment via HTTP",
                    "data": "N/A",
                    "expectedResult": "Connection to the service is established and the service returns correct payment data"
                }
            ],
            "components": [
                "Technology-demo-app-payment"
            ],
            "requirements": [
                "NET-125"
            ],
            "techSpecs": [
                "NET-128"
            ],
            "bugs": []
        }
    }
}
"""

    protected IPipelineSteps steps
    protected GitUtil git
    protected JiraUseCase jiraUseCase

    protected Map data = [:]

    Project(IPipelineSteps steps) {
        this.steps = steps

        this.data.build = [
            hasFailingTests       : false,
            hasUnexecutedJiraTests: false
        ]
    }

    Project init() {
        this.data.buildParams = this.loadBuildParams(steps)
        this.data.metadata = this.loadMetadata(METADATA_FILE_NAME)
        return this
    }

    Project load(GitUtil git, JiraUseCase jiraUseCase) {
        this.git = git
        this.jiraUseCase = jiraUseCase

        this.data.git = [ commit: git.getCommit(), url: git.getURL() ]
        this.data.jira = this.loadJiraData(this.data.metadata.id)
        this.data.jira.project.version = this.loadJiraDataProjectVersion()
        this.data.jira.bugs = this.loadJiraDataBugs(this.data.jira.tests)
        this.data.jira = this.cleanJiraDataItems(this.convertJiraDataToJiraDataItems(this.data.jira))
        this.data.jiraResolved = this.resolveJiraDataItemReferences(this.data.jira)

        this.data.jira.docs = this.loadJiraDataDocs()
        this.data.jira.issueTypes = this.loadJiraDataIssueTypes()
        return this
    }

    protected Map cleanJiraDataItems(Map data) {
        // Bump test steps indizes from 0-based to 1-based counting
        data.tests.each { test ->
            test.getValue().steps.each { step ->
                step.index++
            }
        }

        return data
    }

    protected Map convertJiraDataToJiraDataItems(Map data) {
        JiraDataItem.TYPES.each { type ->
            if (data[type] == null) {
                throw new IllegalArgumentException("Error: Jira data does not include references to items of type '${type}'.")
            }

            data[type] = data[type].collectEntries { key, item ->
                return [key, new JiraDataItem(item, type)]
            }
        }

        return data
    }

    List<JiraDataItem> getAutomatedTests(String componentName = null, List<String> testTypes = []) {
        return this.data.jira.tests.findAll { key, testIssue ->
            def result = testIssue.status.toLowerCase() == "done" && testIssue.executionType?.toLowerCase() == "automated"

            if (result && componentName) {
                result = testIssue.getResolvedComponents()
                    .collect { it.name.toLowerCase() }
                    .contains(componentName.toLowerCase())
            }

            if (result && testTypes) {
                result = testTypes.collect { it.toLowerCase() }.contains(testIssue.testType.toLowerCase())
            }

            return result
        }.values() as List
    }

    Map getEnumDictionary(String name) {
        return this.data.jira.project.enumDictionary[name]
    }

    Map getProjectProperties() {
        return this.data.jira.project.projectProperties
    }

    List<JiraDataItem> getAutomatedTestsTypeAcceptance(String componentName = null) {
        return this.getAutomatedTests(componentName, [TestType.ACCEPTANCE])
    }

    List<JiraDataItem> getAutomatedTestsTypeInstallation(String componentName = null) {
        return this.getAutomatedTests(componentName, [TestType.INSTALLATION])
    }

    List<JiraDataItem> getAutomatedTestsTypeIntegration(String componentName = null) {
        return this.getAutomatedTests(componentName, [TestType.INTEGRATION])
    }

    List<JiraDataItem> getAutomatedTestsTypeUnit(String componentName = null) {
        return this.getAutomatedTests(componentName, [TestType.UNIT])
    }

    Map getBuildParams() {
        return this.data.buildParams
    }

    static List<String> getBuildEnvironment(IPipelineSteps steps, boolean debug = false) {
        def params = loadBuildParams(steps)

        return [
            "DEBUG=${debug}",
            "MULTI_REPO_BUILD=true",
            "MULTI_REPO_ENV=${params.targetEnvironment}",
            "MULTI_REPO_ENV_TOKEN=${params.targetEnvironmentToken}",
            "RELEASE_PARAM_CHANGE_ID=${params.changeId}",
            "RELEASE_PARAM_CHANGE_DESC=${params.changeDescription}",
            "RELEASE_PARAM_CONFIG_ITEM=${params.configItem}",
            "RELEASE_PARAM_VERSION=${params.version}",
            "RELEASE_STATUS_JIRA_ISSUE_KEY=${params.releaseStatusJiraIssueKey}",
            "SOURCE_CLONE_ENV=${params.sourceEnvironmentToClone}",
            "SOURCE_CLONE_ENV_TOKEN=${params.sourceEnvironmentToCloneToken}"
        ]
    }

    List getCapabilities() {
        return this.data.metadata.capabilities
    }

    Object getCapability(String name) {
        def entry = this.getCapabilities().find { it instanceof Map ? it.find { it.key == name } : it == name }
        if (entry) {
            return entry instanceof Map ? entry[name] : true
        }

        return null
    }

    List<JiraDataItem> getBugs() {
        return this.data.jira.bugs.values() as List
    }

    List<JiraDataItem> getComponents() {
        return this.data.jira.components.values() as List
    }

    String getDescription() {
        return this.data.metadata.description
    }

    List<Map> getDocumentTrackingIssues() {
        return this.data.jira.docs.values() as List
    }

    List<Map> getDocumentTrackingIssues(List<String> labels) {
        def result = []

        labels.each { label ->
            this.getDocumentTrackingIssues().each { issue ->
                if (issue.labels.collect { it.toLowerCase() }.contains(label.toLowerCase())) {
                    result << [key: issue.key, status: issue.status]
                }
            }
        }

        return result.unique()
    }

    List<Map> getDocumentTrackingIssuesNotDone(List<String> labels) {
        return this.getDocumentTrackingIssues(labels).findAll { !it.status.equalsIgnoreCase("done") }
    }

    Map getGitData() {
        return this.data.git
    }

    protected URI getGitURLFromPath(String path, String remote = "origin") {
        if (!path?.trim()) {
            throw new IllegalArgumentException("Error: unable to get Git URL. 'path' is undefined.")
        }

        if (!path.startsWith(this.steps.env.WORKSPACE)) {
            throw new IllegalArgumentException("Error: unable to get Git URL. 'path' must be inside the Jenkins workspace: ${path}")
        }

        if (!remote?.trim()) {
            throw new IllegalArgumentException("Error: unable to get Git URL. 'remote' is undefined.")
        }

        def result = null

        this.steps.dir(path) {
            result = this.steps.sh(
                label: "Get Git URL for repository at path '${path}' and origin '${remote}'",
                script: "git config --get remote.${remote}.url",
                returnStdout: true
            ).trim()
        }

        return new URIBuilder(result).build()
    }

    List<JiraDataItem> getEpics() {
        return this.data.jira.epics.values() as List
    }

    String getId() {
        return this.data.jira.project.id
    }

    Map getJiraFieldsForIssueType(String issueTypeName) {
        return this.data.jira.issueTypes[issueTypeName]?.fields ?: [:]
    }

    String getKey() {
        return this.data.metadata.id
    }

    List<JiraDataItem> getMitigations() {
        return this.data.jira.mitigations.values() as List
    }

    String getName() {
        return this.data.metadata.name
    }

    List<Map> getRepositories() {
        return this.data.metadata.repositories
    }

    List<JiraDataItem> getRisks() {
        return this.data.jira.risks.values() as List
    }

    Map getServices() {
        return this.data.metadata.services
    }

    List<JiraDataItem> getSystemRequirements(String componentName = null, List<String> gampTopics = []) {
        return this.data.jira.requirements.findAll { key, req ->
            def result = true

            if (result && componentName) {
                result = req.getResolvedComponents().collect { it.name.toLowerCase() }.contains(componentName.toLowerCase())
            }

            if (result && gampTopics) {
                result = gampTopics.collect { it.toLowerCase() }.contains(req.gampTopic.toLowerCase())
            }

            return result
        }.values() as List
    }

    List<JiraDataItem> getSystemRequirementsTypeAvailability(String componentName = null) {
        return this.getSystemRequirements(componentName, [GampTopic.AVAILABILITY_REQUIREMENT])
    }

    List<JiraDataItem> getSystemRequirementsTypeConstraints(String componentName = null) {
        return this.getSystemRequirements(componentName, [GampTopic.CONSTRAINT])
    }

    List<JiraDataItem> getSystemRequirementsTypeFunctional(String componentName = null) {
        return this.getSystemRequirements(componentName, [GampTopic.FUNCTIONAL_REQUIREMENT])
    }

    List<JiraDataItem> getSystemRequirementsTypeInterfaces(String componentName = null) {
        return this.getSystemRequirements(componentName, [GampTopic.INTERFACE_REQUIREMENT])
    }

    List<JiraDataItem> getTechnicalSpecifications(String componentName = null) {
        return this.data.jira.techSpecs.findAll { key, techSpec ->
            def result = true

            if (result && componentName) {
                result = techSpec.getResolvedComponents().collect { it.name.toLowerCase() }.contains(componentName.toLowerCase())
            }

            return result
        }.values() as List
    }

    List<JiraDataItem> getTests() {
        return this.data.jira.tests.values() as List
    }

    String getOpenShiftApiUrl() {
        return "N/A"
    }

    boolean hasCapability(String name) {
        def collector = {
            return (it instanceof Map) ? it.keySet().first().toLowerCase() : it.toLowerCase()
        }

        return this.capabilities.collect(collector).contains(name.toLowerCase())
    }

    boolean hasFailingTests() {
        return this.data.build.hasFailingTests
    }

    boolean hasUnexecutedJiraTests() {
        return this.data.build.hasUnexecutedJiraTests
    }

    static boolean isTriggeredByChangeManagementProcess(steps) {
        def changeId = steps.env.changeId?.trim()
        def configItem = steps.env.configItem?.trim()
        return changeId && configItem
    }

    static Map loadBuildParams(IPipelineSteps steps) {
        def releaseStatusJiraIssueKey = steps.env.releaseStatusJiraIssueKey?.trim()
        if (isTriggeredByChangeManagementProcess(steps) && !releaseStatusJiraIssueKey) {
            throw new IllegalArgumentException("Error: unable to load build param 'releaseStatusJiraIssueKey': undefined")
        }

        def version = steps.env.version?.trim() ?: "WIP"
        def targetEnvironment = steps.env.environment?.trim() ?: "dev"
        def targetEnvironmentToken = targetEnvironment[0].toUpperCase()
        def sourceEnvironmentToClone = steps.env.sourceEnvironmentToClone?.trim() ?: targetEnvironment
        def sourceEnvironmentToCloneToken = sourceEnvironmentToClone[0].toUpperCase()

        def changeId = steps.env.changeId?.trim() ?: "${version}-${targetEnvironment}"
        def configItem = steps.env.configItem?.trim() ?: "UNDEFINED"
        def changeDescription = steps.env.changeDescription?.trim() ?: "UNDEFINED"

        return [
            changeDescription            : changeDescription,
            changeId                     : changeId,
            configItem                   : configItem,
            releaseStatusJiraIssueKey    : releaseStatusJiraIssueKey,
            sourceEnvironmentToClone     : sourceEnvironmentToClone,
            sourceEnvironmentToCloneToken: sourceEnvironmentToCloneToken,
            targetEnvironment            : targetEnvironment,
            targetEnvironmentToken       : targetEnvironmentToken,
            version                      : version
        ]
    }

    protected Map loadJiraData(String projectKey) {
        return new JsonSlurperClassic().parseText(TEMP_FAKE_JIRA_DATA)
    }

    protected Map loadJiraDataBugs(Map tests) {
        if (!this.jiraUseCase) return [:]
        if (!this.jiraUseCase.jira) return [:]

        def jqlQuery = [
            jql: "project = ${this.data.jira.project.key} AND issuetype = Bug AND status != Done",
            expand: [],
            fields: ["assignee", "duedate", "issuelinks", "status", "summary"]
        ]

        def jiraBugs = this.jiraUseCase.jira.getIssuesForJQLQuery(jqlQuery) ?: []
        return jiraBugs.collectEntries { jiraBug ->
            def bug = [
                key      : jiraBug.key,
                name     : jiraBug.fields.summary,
                assignee : jiraBug.fields.assignee ? [jiraBug.fields.assignee.displayName, jiraBug.fields.assignee.name, jiraBug.fields.assignee.emailAddress].find { it != null } : "Unassigned",
                dueDate  : "", // TODO: currently unsupported for not being enabled on a Bug issue
                status   : jiraBug.fields.status.name
            ]

            def testKeys = []
            if (jiraBug.fields.issuelinks) {
                testKeys = jiraBug.fields.issuelinks.findAll { it.type.name == "Blocks" && it.outwardIssue && it.outwardIssue.fields.issuetype.name == "Test" }.collect { it.outwardIssue.key }
            }

            // Add relations from bug to tests
            bug.tests = testKeys

            // Add relations from tests to bug
            testKeys.each { testKey ->
                if (!tests[testKey].bugs) {
                    tests[testKey].bugs = []
                }

                tests[testKey].bugs << bug.key
            }

            return [jiraBug.key, bug]
        }
    }

    protected Map loadJiraDataProjectVersion() {
        if (!this.jiraUseCase) return [:]
        if (!this.jiraUseCase.jira) return [:]

        return this.jiraUseCase.jira.getVersionsForProject(this.data.jira.project.key).find { version ->
            this.buildParams.version == version.value
        }
    }

    protected Map loadJiraDataDocs() {
        if (!this.jiraUseCase) return [:]
        if (!this.jiraUseCase.jira) return [:]

        def jqlQuery = [jql: "project = ${this.data.jira.project.key} AND issuetype = '${JiraUseCase.IssueTypes.DOCUMENTATION_TRACKING}'"]

        def jiraIssues = this.jiraUseCase.jira.getIssuesForJQLQuery(jqlQuery)
        if (jiraIssues.isEmpty()) {
            throw new IllegalArgumentException("Error: Jira data does not include references to items of type '${JiraDataItem.TYPE_DOCS}'.")
        }

        return jiraIssues.collectEntries { jiraIssue ->
            [
                jiraIssue.key,
                [
                    key        : jiraIssue.key,
                    name       : jiraIssue.fields.summary,
                    description: jiraIssue.fields.description,
                    status     : jiraIssue.fields.status.name,
                    labels     : jiraIssue.fields.labels
                ]
            ]
        }
    }

    protected Map loadJiraDataIssueTypes() {
        if (!this.jiraUseCase) return [:]
        if (!this.jiraUseCase.jira) return [:]

        def jiraIssueTypes = this.jiraUseCase.jira.getIssueTypes(this.data.jira.project.key)
        return jiraIssueTypes.values.collectEntries { jiraIssueType ->
            [
                jiraIssueType.name,
                [
                    id     : jiraIssueType.id,
                    name   : jiraIssueType.name,
                    fields : this.jiraUseCase.jira.getIssueTypeMetadata(this.data.jira.project.key, jiraIssueType.id).values.collectEntries { value ->
                        [
                            value.name,
                            [
                                id:   value.fieldId,
                                name: value.name
                            ]
                        ]
                    }
                ]
            ]
        }
    }

    protected Map loadMetadata(String filename = METADATA_FILE_NAME) {
        if (filename == null) {
            throw new IllegalArgumentException("Error: unable to parse project meta data. 'filename' is undefined.")
        }

        def file = Paths.get(this.steps.env.WORKSPACE, filename).toFile()
        if (!file.exists()) {
            throw new RuntimeException("Error: unable to load project meta data. File '${this.steps.env.WORKSPACE}/${filename}' does not exist.")
        }

        def result = new Yaml().load(file.text)

        // Check for existence of required attribute 'id'
        if (!result?.id?.trim()) {
            throw new IllegalArgumentException("Error: unable to parse project meta data. Required attribute 'id' is undefined.")
        }

        // Check for existence of required attribute 'name'
        if (!result?.name?.trim()) {
            throw new IllegalArgumentException("Error: unable to parse project meta data. Required attribute 'name' is undefined.")
        }

        if (result.description == null) {
            result.description = ""
        }

        if (result.repositories == null) {
            result.repositories = []
        }

        result.repositories.eachWithIndex { repo, index ->
            // Check for existence of required attribute 'repositories[i].id'
            if (!repo.id?.trim()) {
                throw new IllegalArgumentException("Error: unable to parse project meta data. Required attribute 'repositories[${index}].id' is undefined.")
            }

            repo.data = [:]
            repo.data.documents = [:]

            // Set repo type, if not provided
            if (!repo.type?.trim()) {
                repo.type = MROPipelineUtil.PipelineConfig.REPO_TYPE_ODS_CODE
            }

            // Resolve repo URL, if not provided
            if (!repo.url?.trim()) {
                this.steps.echo("Could not determine Git URL for repo '${repo.id}' from project meta data. Attempting to resolve automatically...")

                def gitURL = this.getGitURLFromPath(this.steps.env.WORKSPACE, "origin")
                if (repo.name?.trim()) {
                    repo.url = gitURL.resolve("${repo.name}.git").toString()
                    repo.remove("name")
                } else {
                    repo.url = gitURL.resolve("${result.id.toLowerCase()}-${repo.id}.git").toString()
                }

                this.steps.echo("Resolved Git URL for repo '${repo.id}' to '${repo.url}'")
            }

            // Resolve repo branch, if not provided
            if (!repo.branch?.trim()) {
                this.steps.echo("Could not determine Git branch for repo '${repo.id}' from project meta data. Assuming 'master'.")
                repo.branch = "master"
            }
        }

        if (result.capabilities == null) {
            result.capabilities = []
        }

        // TODO move me to the LeVA documents plugin
        def levaDocsCapabilities = result.capabilities.findAll { it instanceof Map && it.containsKey("LeVADocs") }
        if (levaDocsCapabilities) {
            if (levaDocsCapabilities.size() > 1) {
                throw new IllegalArgumentException("Error: unable to parse project metadata. More than one LeVADoc capability has been defined.")
            }

            def levaDocsCapability = levaDocsCapabilities.first()

            def gampCategory = levaDocsCapability.LeVADocs?.GAMPCategory
            if (!gampCategory) {
                throw new IllegalArgumentException("Error: LeVADocs capability has been defined but contains no GAMPCategory.")
            }

            def templatesVersion = levaDocsCapability.LeVADocs?.templatesVersion
            if (!templatesVersion) {
                levaDocsCapability.LeVADocs.templatesVersion = "1.0"
            }
        }

        return result
    }

    public void reportPipelineStatus(Throwable error) {
        if (!this.jiraUseCase) return
        this.jiraUseCase.updateJiraReleaseStatusIssue(error)
    }

    @NonCPS
    protected Map resolveJiraDataItemReferences(Map data) {
        def result = [:]

        data.each { type, values ->
            if (type == "project") {
                return
            }

            result[type] = [:]

            values.each { key, item ->
                result[type][key] = [:]

                JiraDataItem.TYPES.each { referenceType ->
                    if (item.containsKey(referenceType)) {
                        result[type][key][referenceType] = []

                        item[referenceType].eachWithIndex { referenceKey, index ->
                            result[type][key][referenceType][index] = data[referenceType][referenceKey]
                        }
                    }
                }
            }
        }

        return result
    }

    void setHasFailingTests(boolean status) {
        this.data.build.hasFailingTests = status
    }

    void setHasUnexecutedJiraTests(boolean status) {
        this.data.build.hasUnexecutedJiraTests = status
    }

    String toString() {
        // Don't serialize resolved Jira data items
        def result = this.data.subMap(["build", "buildParams", "git", "jira", "metadata"])

        // Don't serialize temporarily stored document artefacts
        result.metadata.repositories.each { repo ->
            repo.data.documents = [:]
        }

        return JsonOutput.toJson(result)
    }
}
