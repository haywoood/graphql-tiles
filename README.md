# Tiles

[Live app here](https://public-dcnqaruenz.now.sh/)

Drawing with blocks, but mostly an exercise to try some technical things out.

<img src="https://static-avnbmbcpey.now.sh/img/Screen%20Shot%202018-09-11%20at%2012.26.48%20AM.png" width="200" />

### About 

This app is a quick example of how to build a stateless UI, with a local app db that's queryable using GraphQL. The app uses Artemis for data normalization, and GraphQL doc creation / parsing. 

A crucial part to this stack along side the central source of truth is the ability for components, no matter their depth in the view tree, to query for any data they might need. This makes scaling out large UI's a lot easier. When bugs crop up, being able to locate the view with the bug and have the whole world there saves a lot of time. While passing props seems harmless and functional on the surface, you are coupling components to their parents. Often times if you're looking at a component's `data` prop, who knows where that data came from, was it munged? It's better to do the work up front to empower components with the ability to query for their needs.

Because it's stateless, undo/redo capability has been implemented for free. Clojure's persistent data structures make copying the DB on change inexpensive.

### Running locally

- Clone the repo
- CD into the directory and run `npm install`
- Start the development server `npm run start`
- Visit [http://localhost:3449](http://localhost:3449)

To connect to the REPL using your editor:

- Connect to the NREPL server at localhost:4342
- When connected, run `(shadow/nrepl-select :app)`
- Refresh the app in the browser and you should see a message saying `js runtime connected`
