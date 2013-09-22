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
            // Select Match Item (css)
            $(this).siblings().removeClass("match-item-selected");
            $(this).addClass("match-item-selected");

            // Emit event to inform listeners of which Match has been selected
            thisView.trigger("selection:match", thisView._matchesCollection.at($(this).index() -1));
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

                // Prepare the "Status Icon"
                switch(match.get("status")) {
                    case 1: templateData.statusIcon = "<i class=\"icon-flag\"></i>"; break;
                    case 3: templateData.statusIcon = "<i class=\"icon-user\"></i>"; break;
                    case 4: templateData.statusIcon = "<i class=\"icon-cut\"></i>"; break;
                    case 8:
                        switch(match.get("result")) {
                            case "won": templateData.statusIcon = "<i class=\"icon-smile\"></i>"; break;
                            case "lost": templateData.statusIcon = "<i class=\"icon-frown\"></i>"; break;
                            case "draw": templateData.statusIcon = "<i class=\"icon-meh\"></i>"; break;
                        }
                        break;
                }

                // Prepare "id" and "players"
                templateData.id = typeof(match.get("id")) === "string" ? match.get("id").substr(0, 20) + "..." : "UNDEF";
                templateData.players = typeof(match.get("players")) === "number" ? match.get("players") : -1;

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