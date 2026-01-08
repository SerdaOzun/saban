<script lang="ts">
	import { backendUrl } from '../util/SabanConfig';
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { goto } from '$app/navigation';
	import { Languages } from '../data/Languages';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import SearchableCombobox from './SearchableCombobox.svelte';
	import { AudioRecorder } from '../util/Recorder';
	import { onDestroy } from 'svelte';
	import { toast, Toaster } from 'svelte-sonner';

	let {
		word,
		selectedLanguage,
		showDialog = $bindable(),
		requestId,
		fetchRequests
	}: {
		word: string;
		selectedLanguage: string;
		showDialog: boolean;
		requestId: number;
		fetchRequests: () => void;
	} = $props();

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

	async function uploadRequestedAudio() {
		if (!audioBlob || !selectedLanguage || !word) return;

		const formData = new FormData();
		formData.append('file', audioBlob, 'audio.webm');

		try {
			const response = await fetch(`${backendUrl}/gui/request/upload/${requestId}`, {
				method: 'POST',
				body: formData,
				credentials: 'include'
			});

			if (response.status === 401) return goto('/login');

			if (response.ok) {
				reset();
				toast.success('Pronunciation saved', { description: '' });
				fetchRequests();
				showDialog = false;
			}
		} catch (err) {
			console.error('Upload failed:', err);
			serverResponse = `Failed to upload audio: ${err}`;
		}
	}
</script>

<Dialog.Root bind:open={showDialog}>
	<Dialog.Content>
		<Dialog.Header>
			<Dialog.Title>Pronounce Request</Dialog.Title>
		</Dialog.Header>
		<form>
			<div class="grid w-full items-center gap-4">
				<div class="flex flex-col space-y-1.5">
					<Label class="text-lg font-semibold">Word/Phrase</Label>
					<Label id="name">{word}</Label>
				</div>
				<div class="flex flex-col space-y-1.5">
					<Label class="text-lg font-semibold">Language</Label>
					<Label id="lang">{selectedLanguage}</Label>
				</div>
				<div class="flex flex-col space-y-1.5">
					<button
						onclick={recording ? stopRecording : startRecording}
						class="w-fit px-4 py-2 {recording ? 'bg-red-500' : 'bg-blue-500'} rounded-lg text-white"
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
		{#if serverResponse}
			<Label class="self-center">{serverResponse}</Label>
		{/if}
		<Button
			onclick={uploadRequestedAudio}
			class="w-fit justify-end"
			disabled={selectedLanguage === null || word === '' || word === null || audioBlob == null}
			>Send
		</Button>
	</Dialog.Content>
</Dialog.Root>

<Toaster/>
