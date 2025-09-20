<script lang="ts">
    import * as Card from "$lib/components/ui/card/index.js";
    import {backendUrl} from "../util/SabanConfig.ts";
    import {Languages} from "../data/Languages.ts";
    import {Button} from "$lib/components/ui/button";
    import SearchableCombobox from "./SearchableCombobox.svelte";
    import {Label} from "$lib/components/ui/label";
    import {Input} from "$lib/components/ui/input";
    import {goto} from "$app/navigation";

    let {word = $bindable("")}: {
        word?: string;
    } = $props();

    let selectedLanguage: string | null = $state(null)
    let mediaRecorder: MediaRecorder;
    let chunks: Blob[] = [];
    let stream: MediaStream;
    let recording: boolean = $state(false);
    let elapsedTime: number = $state(0);
    let interval: NodeJS.Timeout;
    let audioUrl: string | null = $state(null);
    let audioBlob: Blob | null = $state(null);
    let maxRecordingTime = 10;

    async function startRecording() {
        audioUrl = null;
        stream = await navigator.mediaDevices.getUserMedia({audio: true});
        mediaRecorder = new MediaRecorder(stream);
        chunks = [];
        elapsedTime = 0;
        recording = true;
        mediaRecorder.start();

        interval = setInterval(() => {
            elapsedTime++;
            if (elapsedTime >= maxRecordingTime) {
                stopRecording()
            }
        }, 1000);

        mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) {
                chunks.push(event.data);
            }
        };

        mediaRecorder.onstop = () => {
            clearInterval(interval);
            recording = false;
            const blob = new Blob(chunks, {type: "audio/ogg; codecs=opus"});
            audioUrl = URL.createObjectURL(blob);
            audioBlob = blob;
        };
    }

    function stopRecording() {
        if (mediaRecorder && recording) {
            mediaRecorder.stop();
            stream.getTracks().forEach(track => track.stop());
        }
    }

    async function uploadAudio() {
        if (audioBlob === null || selectedLanguage === null || word === null) return;

        const formData = new FormData();
        formData.append("file", audioBlob, "audio.webm");

        try {
            const response = await fetch(`${backendUrl}/gui/pronunciation/${selectedLanguage}/${word}`, {
                method: "POST",
                body: formData,
                credentials: "include"
            });

            if (response.status === 401) {
                await goto("/login")
            }

            if (response.ok) {
                // todo erfolg dem user anzeigen
                const result = await response.text();
            }
        } catch (error) {
            console.error("Upload failed:", error);
        }
    }
</script>

<Card.Root class="w-[350px]">
    <Card.Header>
        <Card.Title>Record Audio</Card.Title>
        <Card.Description>Choose word and language</Card.Description>
    </Card.Header>
    <Card.Content>
        <form>
            <div class="grid w-full items-center gap-4">
                <div class="flex flex-col space-y-1.5">
                    <Label>Word</Label>
                    <Input id="name" placeholder="word" bind:value={word}/>
                </div>
                <div class="flex flex-col space-y-1.5">
                    <Label>Language</Label>
                    <SearchableCombobox options={Object.values(Languages)} bind:selected={selectedLanguage}/>
                </div>
                <div class="flex flex-col space-y-1.5">
                    <button onclick={recording ? stopRecording : startRecording}
                            class="px-4 py-2 {recording ? 'bg-red-500' : 'bg-blue-500' } text-white rounded-lg">
                        {recording ? 'Stop' : 'Record'}
                    </button>

                    {#if recording}
                        <p>Recording... {elapsedTime}s</p>
                    {/if}
                </div>
                <div>
                    {#if audioUrl}
                        <audio controls>
                            <source src={audioUrl} type="audio/webm"/>
                            Your browser does not support the audio element.
                        </audio>
                    {/if}
                </div>
            </div>
        </form>
    </Card.Content>
    <Card.Footer class="flex justify-between">
        <Button onclick={uploadAudio}
                disabled={selectedLanguage === null || word === "" || word === null || audioBlob == null}>send
        </Button>
    </Card.Footer>
</Card.Root>


<main class="p-4 flex flex-row gap-4 items-center">


</main>
