<script lang="ts">
    import Check from "lucide-svelte/icons/check";
    import ChevronsUpDown from "lucide-svelte/icons/chevrons-up-down";
    import {tick} from "svelte";
    import * as Command from "$lib/components/ui/command/index.js";
    import * as Popover from "$lib/components/ui/popover/index.js";
    import {Button} from "$lib/components/ui/button/index.js";
    import {cn} from "$lib/utils.js";

    let {options, description = "Search...", selected = $bindable()}: {
        options: Array<string>,
        description?: string,
        selected: string | null
    } = $props();

    let open = $state(false);
    let triggerRef = $state<HTMLButtonElement>(null!);

    const selectedValue = $derived(
        options.find((f) => f === selected) ?? description
    );

    // We want to refocus the trigger button when the user selects
    // an item from the list so users can continue navigating the
    // rest of the form with the keyboard.
    function closeAndFocusTrigger() {
        open = false;
        tick().then(() => {
            triggerRef.focus();
        });
    }
</script>

<Popover.Root bind:open>
    <Popover.Trigger bind:ref={triggerRef}>
        {#snippet child({props})}
            <Button
                    variant="outline"
                    class="justify-between"
                    {...props}
                    role="combobox"
                    aria-expanded={open}
            >
                {selectedValue || "Select..."}
                <ChevronsUpDown class="opacity-50"/>
            </Button>
        {/snippet}
    </Popover.Trigger>
    <Popover.Content class="p-0">
        <Command.Root>
            <Command.Input placeholder="Search..." class="h-9"/>
            <Command.List>
                <Command.Empty>No framework found.</Command.Empty>
                <Command.Group>
                    {#each options as option}
                        <Command.Item value={option} onSelect={() => { selected = option; closeAndFocusTrigger(); }}>
                            <Check class={cn(selected !== option&& "text-transparent")}/>
                            {option}
                        </Command.Item>
                    {/each}
                </Command.Group>
            </Command.List>
        </Command.Root>
    </Popover.Content>
</Popover.Root>