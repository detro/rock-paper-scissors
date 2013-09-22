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
        this.$el.delegate("a", "click", function(e) {
            thisView.selectedMatchesType = e.currentTarget.className;
            thisView.trigger("change:selectedMatchesType", thisView.selectedMatchesType);

            e.preventDefault();
        });
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
    _matchesCollection : null,
    initialize : function(options) {
        // Set Matches Collection, if provided
        if (options.matchesCollection) {
            this.setMatchesCollection(options.matchesCollection);
        }
    },
    render : function() {
        var thisView = this;

        if (this._matchesCollection !== null) {
            // Render the List Heading
            this.$el.html("<div class=\"heading\"><h2>"+ this._matchesCollection.type  +" matches</h2></div>");

            // Render the List Match Items
            this._matchesCollection.each(function(match) {
                var statusIcon,
                    id;

                // Prepare the "Status Icon"
                switch(match.get("status")) {
                    case 1:
                        statusIcon = "<i class=\"icon-flag\"></i>";
                        break;
                    case 3:
                        statusIcon = "<i class=\"icon-user\"></i>";
                        break;
                    case 4:
                        statusIcon = "<i class=\"icon-cut\"></i>";
                        break;
                    case 8:
                        switch(match.get("result")) {
                            case "won":
                                statusIcon = "<i class=\"icon-smile\"></i>";
                                break;
                            case "lost":
                                statusIcon = "<i class=\"icon-frown\"></i>";
                                break;
                            case "draw":
                                statusIcon = "<i class=\"icon-meh\"></i>";
                                break;
                        }
                        break;
                }

                // Prepare ID
                if (match.get("id")) {
                    id = match.get("id").substr(0, 20) + "...";
                }

                // Render Match Item
                thisView.$el.append("<div class=\"match-item pure-g\">"
                    + "<div class=\"match-status pure-u-1-6\">"
                    + statusIcon
                    + "</div>"
                    + "<div class=\"match-info pure-u-5-6\">"
                    + "<ul>"
                    + "<li><strong>Match ID:</strong> <span class=\"match-id\">"+ id +"</span></li>"
                    + "<li><strong>Players:</strong> <span class=\"match-players\">"+ match.get("players")  +"</span></li>"
                    + "</ul>"
                    + "</div>"
                    + "</div>");
            });
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