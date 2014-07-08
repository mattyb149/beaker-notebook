var _ = require("lodash")
var RecordNotUniqueError = require("../lib/record_not_unique_error");

module.exports = function(app) {
  var Project = app.Models.Project,
      User = app.Models.User;

  return {
    projectIdParam: function(req, res, next, id) {
      req.user.projects()
      .query({where: {id: parseInt(req.params.project_id, 10)}})
      .fetchOne().then(function(project) {
        if (!project) {
          throw new Error('Project not found');
        }
        else {
          req.project = project;
        }
      })
      .done(next, next);
    },

    index: function(req, res, next) {
      Project.findMatching({
        userId: req.user.id,
        filterBy: decodeURIComponent(req.query.filterBy || '')
      })
      .then(res.json.bind(res))
      .catch(next);
    },

    create: function(req, res, next) {
      Project.forge(
        _.extend({},
          _.pick(req.body, "name", "description"),
          {"ownerId": req.user.get('id')}
        )
      )
      .save()
      .then(function(project) {
        res.json(project);
      })
      .catch(next);
    },

    get: function(req, res, next) {
      req.project.save(
        {openedAt: new Date()},
        {patch: true})
      .then(function(project) {
        return project.withNotebooks();
      })
      .then(res.json.bind(res))
      .catch(next);
    },

    update: function(req, res, next) {
      req.project.save(_.pick(req.body,
        'name', 'description'
      ), {patch: true})
      .then(function(project) {
        res.json(project);
      })
      .catch(function(e) {
        if (e instanceof RecordNotUniqueError) {
          return res.status(409).json({ error: e.message });
        }
        else {
          return next(e);
        }
      })
    },

    destroy: function(req, res, next) {
      req.project.destroy()
      .then(function(project) {
        res.json(project);
      })
      .catch(next);
    }
  };
};