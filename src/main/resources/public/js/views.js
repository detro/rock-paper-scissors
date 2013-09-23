var rps = rps || {};
rps.views = rps.views || {};

// View that renders the left-hand side menu
// that allows to chose which "List of Matches" the user is interested in.
rps.views.MatchesMenu = Backbone.View.extend({
    selectedMatchesType : "available",
    initialize : function(options) {
        var thisView = this;

        // Re-render when any of the collections change
        this.listenTo(options.collections["all"], "add remove", this.render);
        this.listenTo(options.collections["mine"], "add remove", this.render);
        this.listenTo(options.collections["available"], "add remove", this.render);
        // Re-render if the list of selected matches changes
        this.listenTo(this, "change:selectedMatchesType", this.render);

        // Bind clicks on menu entries
        this.$el.on("click", "a", function(e) {
            thisView.selectedMatchesType = e.currentTarget.className;
            thisView.trigger("change:selectedMatchesType", thisView.selectedMatchesType);

            e.preventDefault();
        });

        // First rendering
        this.render();
    },
    render : function() {
        var collections = this.options.collections,
            matchesType;

        for (matchesType in collections) {
            if (collections.hasOwnProperty(matchesType)) {
                // Set number of matches in menu entry
                this.$el.find("a."+ matchesType +" .matches-num").html(collections[matchesType].size());

                // Set CSS for selected matches
                if (matchesType === this.selectedMatchesType) {
                    this.$el.find("a."+ matchesType).parent().addClass("pure-menu-selected");
                } else {
                    this.$el.find("a."+ matchesType).parent().removeClass("pure-menu-selected");
                }
            }
        }
    }
});

// View that renders the List of Matches currently highlighted
// It's not bound to a specific Collection/Model: it can be switched to a specific Collection programmatically
rps.views.MatchesList = Backbone.View.extend({
    _matchItemTemplate : _.template($("#match-item-template").html()),
    _matchesCollection : null,
    _selectedMatchId : null,
    initialize : function(options) {
        var thisView = this;

        // Set Matches Collection, if provided
        if (options.matchesCollection) {
            this.setMatchesCollection(options.matchesCollection);
        }

        // Hover events delegate
        this.$el.on("mouseenter", ".match-item", function(e) {
            $(this).addClass("match-item-hover");
        });
        this.$el.on("mouseleave", ".match-item", function(e) {
            $(this).removeClass("match-item-hover");
        });
        // Click event delegate
        this.$el.on("click", ".match-item", function(e) {
            var selectedMatch = thisView._matchesCollection.at($(this).index() -1);

            // Select Match Item (css)
            $(this).siblings().removeClass("match-item-selected");
            $(this).addClass("match-item-selected");

            // Emit event to inform listeners of which Match has been selected
            thisView._selectedMatchId = selectedMatch.get("id");
            thisView.trigger("select:matchJSON", selectedMatch.toJSON());
        });
    },
    render : function() {
        var i, ilen,
            match, templateData;

        if (this._matchesCollection !== null) {
            // Render the List Heading
            this.$el.html("<div class=\"heading\"><h2>"+ this._matchesCollection.type  +" matches</h2></div>");

            // Render the List Match Items
            for (i = 0, ilen = this._matchesCollection.size(); i < ilen; ++i) {
                match = this._matchesCollection.at(i);
                templateData = {};

                // Prepare "id" and "players"
                templateData.kind = match.get("kind");
                templateData.id = match.get("id").substr(0, 20) + "...";
                templateData.players = match.get("players");
                templateData.selected = this._selectedMatchId === match.get("id");

                // Render Match Item
                this.$el.append(this._matchItemTemplate(templateData));
            }
        }
    },
    setMatchesCollection : function(matchesCollection) {
        // Unbind all events it was listening to
        this.stopListening(this._matchesCollection);
        this._matchesCollection = null;

        // Bind to the new collection
        if (matchesCollection !== null) {
            this._matchesCollection = matchesCollection;
            this.listenTo(this._matchesCollection, "change add remove", this.render);
            this._matchesCollection.trigger("change");
        }
    }
});

// View representing the Current Match
rps.views.CurrentMatch = Backbone.View.extend({
    _matchContentTemplate : _.template($("#match-content-template").html()),
    events : {
        "click #join-button"    : "joinMatch",
        "click #leave-button"   : "leaveMatch",
        "click #restart-button" : "restartMatch"
    },
    initialize : function(options) {
        var thisView = this;

        // Model in the view needs to be constantly refreshed to make
        // sure we know about changes (ex. other player joins)
        function refreshModel() {
            if (thisView.model) {
                thisView.model.fetch();
            }
            setTimeout(refreshModel, thisView.options.game.getRefreshInterval());
        }
        refreshModel();

        // Set weapon on Match when weapon button is clicked
        this.$el.on("click", ".match-weapons button", function() {
            thisView.model.setWeapon( $(this).index() );
        });
    },
    joinMatch : function() { this.model.join(); },
    leaveMatch : function() { this.model.leave(); },
    restartMatch : function() { this.model.restart(); },
    render : function() {
        var templateData;

        if (this.model) {
            templateData = {
                status          : this.model.get("status"),
                kind            : this.model.get("kind"),
                id              : this.model.get("id"),
                players         : this.model.get("players"),
                statusMessage   : rps.views.utils.statusMessageFromMatch(this.model),
                canChooseWeapon : this.model.get("canChooseWeapon"),
                availableWeapons: rps.main.MGame.getWeapons(),
                chosenWeapons   : this.model.get("weapons") || {},
                canJoin         : this.model.get("canJoin"),
                canReset        : this.model.get("canReset"),
                canLeave        : this.model.get("canLeave")
            }

            this.$el.html(this._matchContentTemplate(templateData));
        }
    },
    setMatchJSON : function(matchJSON) {
        // If no model was available before
        if (!this.model) {
            // Store model
            this.model = new rps.models.Match(matchJSON);

            // Listen to model changes from now on
            this.listenTo(this.model, "change", this.render);
            this.model.trigger("change", this.model);
        } else {
            // Change the model's data (not the model itself - we don't need to)
            this.model.set(matchJSON);
        }
    }
});

// View Utilities
rps.views.utils = {};

// Generates a "status message" from the Match status
// TODO Localization?
rps.views.utils.statusMessageFromMatch = function(match) {
    switch(match.get("status")) {
        case 1: return "Ready for Players";
        case 3: return "Waiting for another Player";
        case 4: return "Choosing Weapons";
        case 8:
            if (match.get("result")) {
                switch(match.get("result")) {
                    case "won": return "You WON!";
                    case "lost": return "You LOST!";
                    case "draw": return "Draw - try again";
                }
            } else {
                return "Finished";
            }
    }
};