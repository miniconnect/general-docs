# Contributing

The active repositories generally follow (a simplified version of) the GitFlow approach.
The released version is kept on the `main` branch and is tagged with a version number.
Development takes place on the `develop` branch.

Before contributing, please read the [Code of Conduct](CODE_OF_CONDUCT.md).

## How to Contribute

If you'd like to contribute:

- create a GitHub issue in the most relevant repository using the 'enhancement' label,
  describing the goal you want to achieve and indicating that you would like to implement it yourself
- clone the necessary repositories (we recommend using the [clone script](https://github.com/miniconnect/general-docs/blob/main/maintain/clone.sh) for this)
- create a new feature branch from `develop`:
  `git checkout -b feature/<your-branch> develop`
- commit your changes to this branch (can be multiple commits)
  `git commit -m '<short-description>'`
- don't forget to add tests too if applicable
- push the branch
- [create a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)
  to the `develop` branch, mentioning the GitHub issue you created earlier

Once the pull request has been created, the rest of the process takes place there.
The project maintainer or other participants may request changes before the code is merged.

## Commit Message Guidelines

Please follow these rules when write a commit message:

- don't use any extension (e.g. Conventional Commit markers or issue IDs)
- use only a single line, without a commit body
- use the imperative mode
- start with a capital letter
- don't use punctuation at the end of the sentence
- be concise and informative

Here is an example of what style we have in mind:

> Add the single-parameter substring method to BitString
