
var chart = Highcharts.chart('chart', {
    chart: {
        height: null,
        width:  null
    },

    title: {
        text: $("#chart").data('title')
    },

    xAxis: {
        categories: $("#chart").data('series')[0]["period"]
    },

    yAxis: {
        title: undefined
    },

    series: 
        $("#chart").data('series').map(x => {
            var res = {};
            res["data"] = x["value"];
            res["name"] = x["series_code"];
            return res;
        }),
    
    credits: {
        href: "https://stats.govt.nz",
        text: "Stats NZ"
    }
});