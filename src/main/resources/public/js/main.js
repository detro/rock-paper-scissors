var rps = rps || {};
rps.main = rps.main || {};

rps.main.Collections = {
    "available" : new rps.collections.Matches([], { type : "available" }),
    "mine"      : new rps.collections.Matches([], { type : "mine" }),
    "all"       : new rps.collections.Matches([], { type : "all" })
};

rps.main.MGame = new rps.models.Game();

$(document).ready(function() {
    // Wire buttons in the #nav to create New Matches
    $("#nav #new-match-pvp-button").click(function() { rps.main.MGame.createNewMatch("pvp"); });
    $("#nav #new-match-pvc-button").click(function() { rps.main.MGame.createNewMatch("pvc"); });

    // Create/wire the Matches Menu view
    rps.main.VMatchesMenu = new rps.views.MatchesMenu({
        el : document.getElementById("matches-menu"),
        collections : rps.main.Collections
    });

    // Create/wire the Matches List view
    rps.main.VMatchesList = new rps.views.MatchesList({
        el : document.getElementById("list"),
        matchesCollection : rps.main.Collections[rps.main.VMatchesMenu.selectedMatchesType]
    });

    // Wire Matches Menu view to update the Matches List view
    rps.main.VMatchesMenu.on("change:selectedMatchesType", function(selectedMatchesType) {
        rps.main.VMatchesList.setMatchesCollection(rps.main.Collections[selectedMatchesType]);
    });

    // Create the Current Match view
    rps.main.VCurrentMatch = new rps.views.CurrentMatch({
        el : document.getElementById("main")
    });
    rps.main.VMatchesList.on("selection:match", function(match) {
        rps.main.VCurrentMatch.setModel(match);
    });
});
