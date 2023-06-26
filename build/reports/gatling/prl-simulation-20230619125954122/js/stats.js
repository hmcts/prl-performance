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
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "maxResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "meanResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles2": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles3": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles4": {
        "total": "86",
        "ok": "-",
        "ko": "86"
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
"group_prl-citizen-010-c845b": {
          type: "GROUP",
name: "PRL_Citizen_010_PRLHome",
path: "PRL_Citizen_010_PRLHome",
pathFormatted: "group_prl-citizen-010-c845b",
stats: {
    "name": "PRL_Citizen_010_PRLHome",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "maxResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "meanResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles2": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles3": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles4": {
        "total": "86",
        "ok": "-",
        "ko": "86"
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
"req_prl-citizen-010-aae30": {
        type: "REQUEST",
        name: "PRL_Citizen_010_005_PRLHome",
path: "PRL_Citizen_010_PRLHome / PRL_Citizen_010_005_PRLHome",
pathFormatted: "req_prl-citizen-010-bd134",
stats: {
    "name": "PRL_Citizen_010_005_PRLHome",
    "numberOfRequests": {
        "total": "1",
        "ok": "0",
        "ko": "1"
    },
    "minResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "maxResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "meanResponseTime": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "standardDeviation": {
        "total": "0",
        "ok": "-",
        "ko": "0"
    },
    "percentiles1": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles2": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles3": {
        "total": "86",
        "ok": "-",
        "ko": "86"
    },
    "percentiles4": {
        "total": "86",
        "ok": "-",
        "ko": "86"
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
