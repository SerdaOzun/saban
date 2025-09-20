<script lang="ts">
    import {Languages} from "../data/Languages.ts";
    import {Button} from "$lib/components/ui/button";
    import SearchableCombobox from "./SearchableCombobox.svelte";
    import * as Card from "$lib/components/ui/card/index.js";
    import {Label} from "$lib/components/ui/label";
    import {Input} from "$lib/components/ui/input";
    import {onMount} from "svelte";

    let {word = $bindable("")}: {
        word?: string | null;
    } = $props();

    let selectedLanguage: string | null = $state(null)

</script>

<Card.Root class="w-[350px]">
    <Card.Header>
        <Card.Title>Request pronunciation</Card.Title>
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
                    <Label >Language</Label>
                    <SearchableCombobox options={Object.values(Languages)} bind:selected={selectedLanguage}/>
                </div>
            </div>
        </form>
    </Card.Content>
    <Card.Footer class="flex justify-between">
        <Button disabled={selectedLanguage === null || word === "" || word === null}>send</Button>
    </Card.Footer>
</Card.Root>