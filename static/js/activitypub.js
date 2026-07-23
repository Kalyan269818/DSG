
class ActivityPubActor {
    constructor(profile) {
        this.profile = profile;
    }

    preferredUsername() {
        return this.profile.preferredUsername;
    }


    async inbox() {
        let inbox = await invoke(this.profile.inbox, 'GET');
        return inbox.orderedItems;
    }

    outboxID() {
        return this.profile.outbox;
    }

    async outbox() {
        let outbox = await invoke(this.profile.outbox, 'GET');
        return outbox.orderedItems;
    }

    async post(note, recipients, replyTo) {
        let activity = {
            '@context': 'https://www.w3.org/ns/activitystreams',
            'type': 'Create',
            'actor': this.profile.id,
            'to': recipients,
            'object': {
                '@context': 'https://www.w3.org/ns/activitystreams',
                'type': 'Note',
                'attributedTo': this.profile.id,
                'content': note,
                'to': recipients
            }
        };
        if (replyTo !== null) {
            activity.inReplyTo = replyTo;
        }
        await invoke(this.profile.outbox, 'POST', activity);
    }
}

class MicroBlogNote {

    constructor(mailbox, author, authorID, note) {
        let postTemplate = document.querySelector('#note-template').content;
        let fragment = document.importNode(postTemplate, true);
        let post = fragment.querySelector('.card');
        let authorNameNode = post.querySelector('.username');
        authorNameNode.innerHTML = `${author}`;
        let authorIDNode = post.querySelector('.account-id');

        authorIDNode.innerHTML = `<a href="${authorID}">${authorID}</a>`;
        let noteNode = post.querySelector('.note-text');
        noteNode.innerText = note;
        mailbox.appendChild(post);
    }
}

async function invoke(url, method, body) {
    const CONTENT_TYPE = "application/activity+json; charset=utf-8";
    let headers = {
        "Accept": CONTENT_TYPE,
    };
    if (body !== null) {
        headers["Content-Type"] = CONTENT_TYPE;
    }
    let options = {
        method: method,
        headers: headers
    };
    if (body) {
        options.body = JSON.stringify(body);
    }

    let response = await fetch(url, options);
    if (!response.ok) {
        throw new Error(response.status);
    }
    if (response.status === 201 || response.status === 204) {
        return null;
    }

    const result  = await response.json();
    return result;
}

async function postCallback(event) {
    event.preventDefault();

    let recipientsInput = document.querySelector('#recipients');
    let recipients = recipientsInput.value;
    if (!recipients) {
        recipients = 'https://www.w3.org/ns/activitystreams#Public';
    }
    recipients = recipients.split(/,\s*/);
    for (let i = 0; i < recipients.length; i++) {
        recipients[i]= recipients[i].trim();
    }

    let content = document.querySelector('#new-post');
    let text = content.value;
    let postForm = document.querySelector('#post-form');
    if (text === '' || postForm.actor === null || postForm.actor === undefined) {
        return;
    }

    try {
        event.target.disabled = true;
        await postForm.actor.post(content.value, recipients, null);
        content.value = '';
        recipientsInput.value = '';
    } catch (error) {
        console.log(error)
    } finally {
        event.target.disabled = false;
    }
}

var ACTOR_CACHE = new Map();

async function refreshMailbox(mailbox, actor, htmlElement) {
    let notes;
    switch (mailbox) {
    case 'INBOX':
        notes = await actor.inbox();
        break;
    case 'OUTBOX':
        notes = await actor.outbox();
        break;
    default:
        throw new Error('Unknown mailbox: ' + mailbox);
    }
    if (notes.length === 0) {
        htmlElement.innerHTML = '<p class="text-center">There are no posts in this mailbox, yet.</p>';
        return;
    }

    if (notes.length === htmlElement.numItems) {
        return;
    }

    htmlElement.innerText = '';
    htmlElement.numItems = notes.length;
    for (const item of notes) {
        let profile = ACTOR_CACHE.get(item.actor);
        let username;

        // Prevent the client from trying to fetch resources from localhost if
        // we are connected to a server running locally. Otherwise, this might
        // have unintended consequences.
        if (item.actor.includes("localhost") && !window.location.hostname.includes("localhost")) {
            username = "Unknown";
        } else if (profile === undefined) {
            try {
                profile = await invoke(item.actor, 'GET');
                ACTOR_CACHE.set(item.actor, profile);
                username = profile.preferredUsername;
            } catch (error) {
                // Provide fallback username in cases where we cannot fetch the
                // target profile. (e.g. because it was generated on localhost).
                console.log(error);
                username = "Unknown";
            }
        } else {
            username = profile.preferredUsername;
        }
        new MicroBlogNote(htmlElement, username, item.actor, item.object.content);
    }
}

async function renderHomepage(actor) {
    let actorName = document.querySelector('#actor-name');
    actorName.innerText = '';
    actorName.classList.add('d-none');
    let content = document.querySelector('#content');
    let postForm = document.querySelector('#post-form');
    let mailboxNav = document.querySelector('#mailbox-nav');
    let mailboxContent = document.querySelector('#mailbox-content');

    if (actor === null) {
        content.innerHTML = `
<div class="d-flex p-3 justify-content-center">
    <p>Please sign in with the identity of a MicroBlog user or register a new MicroBlog user to test ActivityPub mailboxes.</p>
</div>`;
        return;
    }

    postForm.actor = actor;
    let inbox = await actor.inbox();
    let outbox = await actor.outbox();
    let inboxContent = document.querySelector('#inbox');
    await refreshMailbox('INBOX', actor, inboxContent);
    let outboxContent = document.querySelector('#outbox');
    await refreshMailbox('OUTBOX', actor, outboxContent);

    postForm.classList.remove('d-none');
    mailboxNav.classList.remove('d-none');
    mailboxContent.classList.remove('d-none');
}
async function renderProfile(actor) {
    let actorName = document.querySelector('#actor-name');
    actorName.innerText = '- ' + actor.preferredUsername() + '\'s Profile';

    let content = document.querySelector('#content');
    let postForm = document.querySelector('#post-form');
    let mailboxNav = document.querySelector('#mailbox-nav');
    let mailboxContent = document.querySelector('#mailbox-content');

    let inboxButton = document.querySelector('#nav-inbox-tab');
    inboxButton.classList.remove('active');
    inboxButton.disabled = true;
    let inboxContent = document.querySelector('#inbox');
    inboxContent.classList.remove('show');

    let outboxButton = document.querySelector('#nav-outbox-tab');
    outboxButton.classList.add('active');
    let outboxContent = document.querySelector('#outbox');
    await refreshMailbox('OUTBOX', actor, outboxContent);

    outboxContent.classList.add('show', 'active');
    mailboxNav.classList.remove('d-none');
    mailboxContent.classList.remove('d-none');
}

async function init() {
    let postButton = document.querySelector('#post-button');
    postButton.addEventListener('click', postCallback);

    let route = window.location.pathname;
    try {
        let profile;
        let actor;
        switch (route) {
            case '/':
                // Check if user is signed in.
                profile = await invoke('/user', 'GET');
                if (profile === null) {
                    renderHomepage(null);
                    return;
                }

                actor = new ActivityPubActor(profile);
                postButton.actorID = actor.outboxID();
                await renderHomepage(actor);
                setInterval(async () => { await renderHomepage(actor)}, 5000)
                break;
            default:
                profile = await invoke(route, 'GET');
                actor = new ActivityPubActor(profile);
                await renderProfile(actor);
                setInterval(async () => { await renderProfile(actor)}, 5000)
                break;
        }

    } catch (error) {
        console.log(error)
        content.innerHTML = `
<div class="d-flex p-3 justify-content-center">
    <p><strong>Page not found</strong></p>
</div>`;
    }
}

window.addEventListener('load', function() {
    init().then(() => {
    }, () => {
    });
});
