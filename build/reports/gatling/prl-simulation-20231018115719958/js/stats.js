var stats = {
    type: "GROUP",
name: "All Requests",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "All Requests",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "maxResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "meanResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles2": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles3": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles4": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 1,
    "percentage": 100
},
    "meanNumberOfRequestsPerSecond": {
        "total": "0.083",
        "ok": "-",
        "ko": "0.083"
    }
},
contents: {
"group_xui-010-homepag-0e4d4": {
          type: "GROUP",
name: "XUI_010_Homepage",
path: "XUI_010_Homepage",
pathFormatted: "group_xui-010-homepag-0e4d4",
stats: {
    "name": "XUI_010_Homepage",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "maxResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "meanResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles2": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles3": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles4": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 1,
    "percentage": 100
},
    "meanNumberOfRequestsPerSecond": {
        "total": "0.083",
        "ok": "-",
        "ko": "0.083"
    }
},
contents: {
"req_xui-010-005-hom-0514a": {
        type: "REQUEST",
        name: "XUI_010_005_Homepage",
path: "XUI_010_Homepage / XUI_010_005_Homepage",
pathFormatted: "req_xui-010-homepag-8dfbb",
stats: {
    "name": "XUI_010_005_Homepage",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "maxResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "meanResponseTime": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles2": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles3": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "percentiles4": {
        "total": "11340",
        "ok": "-",
        "ko": "11340"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 1,
    "percentage": 100
},
    "meanNumberOfRequestsPerSecond": {
        "total": "0.083",
        "ok": "-",
        "ko": "0.083"
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
