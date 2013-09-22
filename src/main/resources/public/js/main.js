var rps = rps || {};
rps.main = rps.main || {};

rps.main.CAvailableMatches = new rps.collections.Matches("available");
rps.main.CMyMatches = new rps.collections.Matches("mine");
rps.main.CAllMatches = new rps.collections.Matches("all");

$(document).ready(function() {
    rps.main.VMatchesMenu = new rps.views.MatchesMenu({
        el : document.getElementById("matches-menu"),
        collections : {
            "all"       : rps.main.CAllMatches,
            "mine"      : rps.main.CMyMatches,
            "available" : rps.main.CAvailableMatches
        }
    });

    rps.main.VMatchesMenu.on("change:selectedMatchesType", function(selectedMatchesType) {
        // TODO
        console.log("Selected Matches Type: " + selectedMatchesType);
    });
});
