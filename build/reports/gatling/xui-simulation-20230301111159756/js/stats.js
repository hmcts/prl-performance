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
        "total": "406",
        "ok": "899",
        "ko": "406"
    },
    "maxResponseTime": {
        "total": "899",
        "ok": "899",
        "ko": "406"
    },
    "meanResponseTime": {
        "total": "653",
        "ok": "899",
        "ko": "406"
    },
    "standardDeviation": {
        "total": "247",
        "ok": "0",
        "ko": "0"
    },
    "percentiles1": {
        "total": "653",
        "ok": "899",
        "ko": "406"
    },
    "percentiles2": {
        "total": "776",
        "ok": "899",
        "ko": "406"
    },
    "percentiles3": {
        "total": "874",
        "ok": "899",
        "ko": "406"
    },
    "percentiles4": {
        "total": "894",
        "ok": "899",
        "ko": "406"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 1,
    "percentage": 50
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
        "total": "1",
        "ok": "0.5",
        "ko": "0.5"
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
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "maxResponseTime": {
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "meanResponseTime": {
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "0",
        "ko": "-"
    },
    "percentiles1": {
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "percentiles2": {
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "percentiles3": {
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "percentiles4": {
        "total": "899",
        "ok": "899",
        "ko": "-"
    },
    "group1": {
    "name": "t < 800 ms",
    "count": 0,
    "percentage": 0
},
    "group2": {
    "name": "800 ms < t < 1200 ms",
    "count": 1,
    "percentage": 100
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
        "total": "0.5",
        "ok": "0.5",
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
        "total": "406",
        "ok": "-",
        "ko": "406"
    },
    "maxResponseTime": {
        "total": "406",
        "ok": "-",
        "ko": "406"
    },
    "meanResponseTime": {
        "total": "406",
        "ok": "-",
        "ko": "406"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "406",
        "ok": "-",
        "ko": "406"
    },
    "percentiles2": {
        "total": "406",
        "ok": "-",
        "ko": "406"
    },
    "percentiles3": {
        "total": "406",
        "ok": "-",
        "ko": "406"
    },
    "percentiles4": {
        "total": "406",
        "ok": "-",
        "ko": "406"
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
        "total": "0.5",
        "ok": "-",
        "ko": "0.5"
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
