var rps = rps || {};
rps.main = rps.main || {};

// Create the Game model
rps.main.MGame = new rps.models.Game();

// When the Game model is ready, everything else can continue
rps.main.MGame.on("ready", function() {

    // Create Collections
    rps.main.Collections = {
        "available" : new rps.collections.Matches([], { type : "available", game : rps.main.MGame }),
        "mine"      : new rps.collections.Matches([], { type : "mine", game : rps.main.MGame  }),
        "all"       : new rps.collections.Matches([], { type : "all", game : rps.main.MGame })
    };

    // Create Views
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
            el      : document.getElementById("main"),
            game    : rps.main.MGame
        });
        rps.main.VMatchesList.on("select:matchJSON", function(matchJSON) {
            rps.main.VCurrentMatch.setMatchJSON(matchJSON);
        });

        // Wire the "#nav-menu-button" that appears on narrow screens (i.e. smartphones)
        $("#nav-menu-button").click(function() {
            if ($("#nav-inner").hasClass("open")) {
                $("#nav-inner").removeClass("open");
            } else {
                $("#nav-inner").addClass("open");
            }
        });
    });

});
