var rps = rps || {};
rps.models = rps.models || {};
rps.collections = rps.collections || {};

// Model of a Match
rps.models.Match = Backbone.Model.extend({
    urlRoot     : "/api/match",
    defaults    : {
        status      : -1,
        players     : 0,
        result      : null
    },
    isEmpty             : function() { return this.get("status") === 1; },
    isWaitingPlayer     : function() { return this.get("status") === 3; },
    isWaitingWeapons    : function() { return this.get("status") === 4; },
    isFinished          : function() { return this.get("status") === 8; },
    join        : function() {
        var thisMatch = this;

        Backbone.ajax({
            url     : this.url(),
            type    : "PUT",
            data    : {
                action  : "join"
            },
            success : function(data) {
                thisMatch.set(data);
                thisMatch.trigger("join:success", thisMatch);
            },
            error   : function() { thisMatch.trigger("join:fail", thisMatch); }
        });
    },
    reset       : function() {
        var thisMatch = this;

        Backbone.ajax({
            url     : this.url(),
            type    : "PUT",
            data    : {
                action  : "reset"
            },
            success : function(data) {
                thisMatch.set(data);
                thisMatch.trigger("reset:success", thisMatch);
            },
            error   : function() { thisMatch.trigger("reset:fail", thisMatch); }
        });
    },
    setWeapon   : function(weaponId) {
        var thisMatch = this;

        Backbone.ajax({
            url     : this.url(),
            type    : "PUT",
            data    : {
                action  : "weapon",
                weapon  : weaponId
            },
            success : function(data) {
                thisMatch.set(data);
                thisMatch.trigger("weapon:success", thisMatch);
            },
            error   : function() { thisMatch.trigger("weapon:fail", thisMatch); }
        });
    },
});

// Collection of Matches
// At construction, the parameter "type" can be provided to
// obtain a specific set of Matches ("mine", "available", "all").
// Default is "all"
rps.collections.Matches = Backbone.Collection.extend({
    model   : rps.models.Match,
    url     : "/api/matches",
    _autoUpdateEnabled  : true,
    _autoUpdateInterval : 1000,
    initialize : function(type) {
        var autoUpdate,
            thisCollection = this;

        // Initialize READ URL
        type = type || "all";
        this.url += "?type=" + type;

        // Begin Auto Update
        autoUpdate = function() {
            if (thisCollection._autoUpdateEnabled) {
                thisCollection.fetch();
            }
            setTimeout(autoUpdate, thisCollection._autoUpdateInterval);
        };
        autoUpdate();
    },
    enableAutoUpdate : function() {
        this._autoUpdateEnabled = true;
    },
    disableAutoUpdate : function() {
        this._autoUpdateEnabled = false;
    }
});

// Game Model
rps.models.Game = function() {
    var weapons,
        thisGame = this;

    // Decorate Game Model with Backbone Events
    _.extend(this, Backbone.Events);

    // Fetch available weapons
    Backbone.ajax({
        url     : "/api/weapons",
        type    : "GET",
        success : function(data) {
            weapons = data;
            thisGame.trigger("ready:weapons", weapons);
        }
    });

    // Weapon specific methods
    this.getWeaponId = function(name) {
        var i, ilen;
        for (i = 0, ilen = weapons.length; i < ilen; ++i) {
            if (weapons[i] === name) {
                return i;
            }
        }

        return null;
    };
    this.getWeaponName = function(id) {
        return (id >= 0 && id < weapons.length) ? weapons[id] : null;
    };
    this.getWeaponsAmount = function() {
        return weapons.length;
    };

    // Create new match
    this.createNewMatch = function(matchKind) {
        matchKind = matchKind || "pvp";

        Backbone.ajax({
            url     : "/api/match",
            type    : "POST",
            data    : {
                kind    : matchKind
            },
            success : function(data) {
                var newMatch = new rps.models.Match(data);
                thisGame.trigger("create:match:success", newMatch);
            },
            error   : function() {
                thisGame.trigger("create:match:fail");
            }
        });
    };

    return this;
};