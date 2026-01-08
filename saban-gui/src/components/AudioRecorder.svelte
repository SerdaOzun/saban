<script lang="ts">
	import { backendUrl } from '../util/SabanConfig';
	import { goto } from '$app/navigation';
	import { Languages } from '../data/Languages';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import SearchableCombobox from './SearchableCombobox.svelte';
	import { AudioRecorder } from '../util/Recorder';
	import { onDestroy } from 'svelte';

	let { word = $bindable(''), selectedLanguage = null } = $props();

	let recorder = new AudioRecorder({
		maxRecordingTime: 10,
		onElapsed: (sec: number) => (elapsedTime = sec),
		onStop: (blob: Blob, url: string) => {
			audioBlob = blob;
			audioUrl = url;
		}
	});

	let recording = $state(false);
	let elapsedTime = $state(0);
	let audioUrl: string | null = $state(null);
	let audioBlob: Blob | null = $state(null);
	let serverResponse: string | null = $state(null);

	onDestroy(() => {
		stopRecording();
	});

	function startRecording() {
		audioBlob = null;
    	audioUrl = null;
		recording = true;
		recorder.start();
	}

	function stopRecording() {
		recording = false;
		recorder.stop();
	}

	function reset() {
		word = '';
		audioBlob = null;
		audioUrl = null;
	}

	async function uploadAudio() {
		if (!audioBlob || !selectedLanguage || !word) return;

		const formData = new FormData();
		formData.append('file', audioBlob, 'audio.webm');

		try {
			const response = await fetch(`${backendUrl}/gui/pronunciation/${selectedLanguage}/${word}`, {
				method: 'POST',
				body: formData,
				credentials: 'include'
			});

			if (response.status === 401) return goto('/login');

			if (response.ok) {
				reset();
				serverResponse = await response.text();
			}
		} catch (err) {
			console.error('Upload failed:', err);
			serverResponse = `Failed to upload audio: ${err}`;
		}
	}
</script>

<Card.Root class="w-[350px]">
	<Card.Header>
		<Card.Title>Record Audio</Card.Title>
		<Card.Description>Choose word or phrase and language</Card.Description>
	</Card.Header>
	<Card.Content>
		<form>
			<div class="grid w-full items-center gap-4">
				<div class="flex flex-col space-y-1.5">
					<Label>Word</Label>
					<Input id="name" placeholder="word" bind:value={word} />
				</div>
				<div class="flex flex-col space-y-1.5">
					<Label>Language</Label>
					<SearchableCombobox options={Object.values(Languages)} bind:selected={selectedLanguage} />
				</div>
				<div class="flex flex-col space-y-1.5">
					<button
						onclick={recording ? stopRecording : startRecording}
						class="px-4 py-2 {recording ? 'bg-red-500' : 'bg-blue-500'} rounded-lg text-white"
					>
						{recording ? 'Stop' : 'Record'}
					</button>

					{#if recording}
						<p>Recording... {elapsedTime}s</p>
					{/if}
				</div>
				<div>
					{#if audioUrl}
						<audio controls>
							<source src={audioUrl} type="audio/webm" />
							Your browser does not support the audio element.
						</audio>
					{/if}
				</div>
			</div>
		</form>
	</Card.Content>
	<Card.Footer class="flex flex-col gap-2">
		{#if serverResponse}
			<Label class="self-center">{serverResponse}</Label>
		{/if}
		<Button
			onclick={uploadAudio}
			class="self-end"
			disabled={selectedLanguage === null || word === '' || word === null || audioBlob == null}
			>Send
		</Button>
	</Card.Footer>
</Card.Root>
