<script lang="ts">
	import { Languages } from '../data/Languages.ts';
	import { Button } from '$lib/components/ui/button';
	import SearchableCombobox from './SearchableCombobox.svelte';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { onMount } from 'svelte';
	import { backendUrl } from '../util/SabanConfig.ts';
	import { toast } from 'svelte-sonner';
	import { Toaster } from '@/components/ui/sonner/index.ts';
	import { goto } from '$app/navigation';

	let {
		word = $bindable('')
	}: {
		word?: string | null;
	} = $props();

	let selectedLanguage: string | null = $state(null);
	let serverResponse: string | null = $state(null);

	async function requestWord() {
		if (!word || !selectedLanguage) return;

		const resp = await fetch(`${backendUrl}/gui/request`, {
			method: 'POST',
			credentials: 'include',
			headers: {
				'Content-type': 'application/json; charset=UTF-8'
			},
			body: JSON.stringify({
				text: word,
				language: selectedLanguage
			})
		});

		if (resp.status === 401) {
			await goto('/login');
		}

		if (resp.ok) {
			toast.success('Request has been saved', {
				description: ''
			});
		}
	}
</script>

<Card.Root class="w-[350px]">
	<Card.Header>
		<Card.Title>Request pronunciation</Card.Title>
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
			</div>
		</form>
	</Card.Content>
	<Card.Footer class="flex w-full flex-col gap-2">
		{#if serverResponse}
			<Label class="self-center">{serverResponse}</Label>
		{/if}

		<Button
			class="self-end"
			onclick={requestWord}
			disabled={selectedLanguage === null || word === '' || word === null}>Send</Button
		>
	</Card.Footer>
</Card.Root>

<Toaster />
