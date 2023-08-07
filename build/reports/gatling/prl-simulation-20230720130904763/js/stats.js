var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "maxResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "meanResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles2": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles3": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles4": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group4": {
    "name": "failed",
    "count": 1,
    "percentage": 100
},
    "meanNumberOfRequestsPerSecond": {
        "total": "1",
        "ok": "-",
        "ko": "1"
    }
},
contents: {
"group_xui-prl-030-sel-ebaa9": {
          type: "GROUP",
name: "XUI_PRL_030_SelectCase",
path: "XUI_PRL_030_SelectCase",
pathFormatted: "group_xui-prl-030-sel-ebaa9",
stats: {
    "name": "XUI_PRL_030_SelectCase",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "maxResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "meanResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles2": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles3": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles4": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group4": {
    "name": "failed",
    "count": 1,
    "percentage": 100
},
    "meanNumberOfRequestsPerSecond": {
        "total": "1",
        "ok": "-",
        "ko": "1"
    }
},
contents: {
"req_xui-prl-040-005-c0cf8": {
        type: "REQUEST",
        name: "XUI_PRL_040_005_SelectIssue",
path: "XUI_PRL_030_SelectCase / XUI_PRL_040_005_SelectIssue",
pathFormatted: "req_xui-prl-030-sel-f73b6",
stats: {
    "name": "XUI_PRL_040_005_SelectIssue",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "maxResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "meanResponseTime": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles2": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles3": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "percentiles4": {
        "total": "493",
        "ok": "-",
        "ko": "493"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group3": {
    "name": "t > 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group4": {
    "name": "failed",
    "count": 1,
    "percentage": 100
},
    "meanNumberOfRequestsPerSecond": {
        "total": "1",
        "ok": "-",
        "ko": "1"
    }
}
    }
}

     }
}

}

function fillStats(stat){
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
