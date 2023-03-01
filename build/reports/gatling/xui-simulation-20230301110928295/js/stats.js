var stats = {
    type: "GROUP",
name: "Global Information",
path: "",
pathFormatted: "group_missing-name-b06d1",
stats: {
    "name": "Global Information",
    "numberOfRequests": {
        "total": "2",
        "ok": "1",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "705",
        "ok": "705",
        "ko": "4457"
    },
    "maxResponseTime": {
        "total": "4457",
        "ok": "705",
        "ko": "4457"
    },
    "meanResponseTime": {
        "total": "2581",
        "ok": "705",
        "ko": "4457"
    },
    "standardDeviation": {
        "total": "1876",
        "ok": "0",
        "ko": "0"
    },
    "percentiles1": {
        "total": "2581",
        "ok": "705",
        "ko": "4457"
    },
    "percentiles2": {
        "total": "3519",
        "ok": "705",
        "ko": "4457"
    },
    "percentiles3": {
        "total": "4269",
        "ok": "705",
        "ko": "4457"
    },
    "percentiles4": {
        "total": "4419",
        "ok": "705",
        "ko": "4457"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 1,
    "percentage": 50
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
    "percentage": 50
},
    "meanNumberOfRequestsPerSecond": {
        "total": "0.333",
        "ok": "0.167",
        "ko": "0.167"
    }
},
contents: {
"req_xui-000-auth-1949a": {
        type: "REQUEST",
        name: "XUI_000_Auth",
path: "XUI_000_Auth",
pathFormatted: "req_xui-000-auth-1949a",
stats: {
    "name": "XUI_000_Auth",
    "numberOfRequests": {
        "total": "1",
        "ok": "1",
        "ko": "0"
    },
    "minResponseTime": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "0",
        "ko": "-"
    },
    "percentiles1": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "percentiles2": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "percentiles3": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "percentiles4": {
        "total": "705",
        "ok": "705",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 1,
    "percentage": 100
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
    "count": 0,
    "percentage": 0
},
    "meanNumberOfRequestsPerSecond": {
        "total": "0.167",
        "ok": "0.167",
        "ko": "-"
    }
}
    },"req_xui-000-getbear-74e18": {
        type: "REQUEST",
        name: "XUI_000_GetBearerToken",
path: "XUI_000_GetBearerToken",
pathFormatted: "req_xui-000-getbear-74e18",
stats: {
    "name": "XUI_000_GetBearerToken",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
    },
    "maxResponseTime": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
    },
    "meanResponseTime": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
    },
    "percentiles2": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
    },
    "percentiles3": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
    },
    "percentiles4": {
        "total": "4457",
        "ok": "-",
        "ko": "4457"
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
        "total": "0.167",
        "ok": "-",
        "ko": "0.167"
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
